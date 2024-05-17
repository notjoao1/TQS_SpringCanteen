package pt.ua.deti.springcanteen.service.impl;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.dto.CustomizeDTO;
import pt.ua.deti.springcanteen.dto.CustomizeIngredientDTO;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.exceptions.InvalidOrderException;
import pt.ua.deti.springcanteen.service.PriceService;


import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class IPriceService implements PriceService {
    private static final Logger logger = LoggerFactory.getLogger(IPriceService.class);

    public float getOrderMenuPrice(OrderMenu orderMenu) {
        logger.info("Getting menu price for OrderMenu with customization -> {}", orderMenu.getCustomization());
        Menu menu = orderMenu.getMenu();
        CustomizeDTO customizeDTO = new Gson().fromJson(orderMenu.getCustomization(), CustomizeDTO.class);
        Long customizedDrinkId = customizeDTO.getCustomized_drink().getItem_id();
        Long customizedMainDishId = customizeDTO.getCustomized_main_dish().getItem_id();

        // Check if the specified drink with a given id exists as an option for that menu
        Optional<Drink> drinkOpt = menu.getDrinkOptions().stream().filter(d -> d.getId().equals(customizedDrinkId)).findFirst();
        // Check if the specified main dish with a given id exists as an option for that menu
        Optional<MainDish> mainDishOpt = menu.getMainDishOptions().stream().filter(m -> m.getId().equals(customizedMainDishId)).findFirst();

        if (drinkOpt.isEmpty()) {
            logger.error("Drink id '{}' not found for this menu.", customizedDrinkId);
            throw new InvalidOrderException("Provided drink does not exist as an option in the given menu.");
        }
        if (mainDishOpt.isEmpty()){
            logger.error("Main dish id '{}' not found for this menu.", customizedMainDishId);
            throw new InvalidOrderException("Provided main dish does not exist as an option in the given menu.");
        }

        Set<CustomizeIngredientDTO> customizedIngredients = customizeDTO.getCustomized_main_dish().getCustomized_ingredients();
        Set<MainDishIngredients> baseIngredients = mainDishOpt.get().getMainDishIngredients();

        return drinkOpt.get().getPrice() + getMainDishPriceBasedOnCustomization(baseIngredients, customizedIngredients);
    }

    
    // iterate over each customized ingredient, and check if extra ingredients were added based on the base ingredients
    // if so, price_of_ingredient = price_per_ingredient * quantity,
    // else, price_of_ingredient = price_per_ingredient * base_quantity
    // also throws exception is an expected base ingredient was not specified
    private float getMainDishPriceBasedOnCustomization(Set<MainDishIngredients> baseIngredients, Set<CustomizeIngredientDTO> customizedIngredients) {
        float mainDishPrice = 0;
        for (MainDishIngredients baseIngredient : baseIngredients) {
            CustomizeIngredientDTO customizeIngredientDTO = customizedIngredients.stream().filter(i -> i.getIngredient_id().equals(baseIngredient.getIngredient().getId()))
                                                .findFirst().orElseThrow(() -> {
                                                    logger.error("Ingredient with id {} and name {} was not specified.", baseIngredient.getIngredient().getId(), baseIngredient.getIngredient().getName());
                                                    return new InvalidOrderException(String.format("You must specify all ingredients in the main dish and their quantity when ordering a certain menu. Ingredient %d '%s' not found.", 
                                                                                                    baseIngredient.getIngredient().getId(), baseIngredient.getIngredient().getName()));
                                                                                                });
            if (customizeIngredientDTO.getQuantity() > baseIngredient.getQuantity()) {
                mainDishPrice = mainDishPrice + customizeIngredientDTO.getQuantity() * baseIngredient.getIngredient().getPrice();
            } else {
                mainDishPrice = mainDishPrice + baseIngredient.getQuantity() * baseIngredient.getIngredient().getPrice();
            }
        }

        return mainDishPrice;
    }

}
