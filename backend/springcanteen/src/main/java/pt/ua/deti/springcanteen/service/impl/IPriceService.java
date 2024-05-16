package pt.ua.deti.springcanteen.service.impl;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.ua.deti.springcanteen.dto.CustomizeDTO;
import pt.ua.deti.springcanteen.dto.CustomizeIngredientDTO;
import pt.ua.deti.springcanteen.dto.CustomizeOrderDTO;
import pt.ua.deti.springcanteen.dto.OrderMenuDTO;
import pt.ua.deti.springcanteen.entities.*;
import pt.ua.deti.springcanteen.service.PriceService;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class IPriceService implements PriceService {

    public float getOrderMenuPrice(OrderMenu orderMenu){
        Menu menu = orderMenu.getMenu();
        CustomizeDTO customizeDTO = new Gson().fromJson(orderMenu.getCustomization(), CustomizeDTO.class);
        Long customizedDrinkId = customizeDTO.getCustomized_drink().getItem_id();
        Long customizedMainDishId = customizeDTO.getCustomized_main_dish().getItem_id();

        // TODO verificar que menu existe (pode ser noutro spot)
        // TODO verificar que ingredientes existem

        // Check if the specified drink with a given id exists for that menu
        Optional<Drink> drinkOpt = menu.getDrinkOptions().stream().filter(d -> d.getId().equals(customizedDrinkId)).findFirst();
        // Check if the specified dish with a given id exists for that menu
        Optional<MainDish> mainDishOpt = menu.getMainDishOptions().stream().filter(m -> m.getId().equals(customizedMainDishId)).findFirst();

        if ( drinkOpt.isEmpty() || mainDishOpt.isEmpty()){
            throw new RuntimeException("SOME ERROR");
        }


        MainDish mainDish = mainDishOpt.get();
        Set<CustomizeIngredientDTO> customizedIngredients = customizeDTO.getCustomized_main_dish().getCustomized_ingredients();

        for (CustomizeIngredientDTO customizedIngredient : customizedIngredients){
            Ingredient ingredient = mainDish.getMainDishIngredients().stream()
                    .filter(i -> i.getId().equals(customizedIngredient.getIngredient_id())).findFirst().orElseThrow(RuntimeException::new)
                    .getIngredient();

        }


        Set<Ingredient> ingredients = mainDish.getMainDishIngredients().stream().filter(i -> i.getId().equals())


        return
                drinkOpt.get().getPrice() + mainDish.getPrice() + mainDish.getMainDishIngredients().stream()
                        .filter(i -> i.getQuantity() > mainDish.getMainDishIngredients().stream().)
                        .map(i -> i.getIngredient().getPrice() * i.getQuantity() - )
                        .reduce(0f, Float::sum)
        ;
    }

}
