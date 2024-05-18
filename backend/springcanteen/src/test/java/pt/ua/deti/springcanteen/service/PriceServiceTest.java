package pt.ua.deti.springcanteen.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.ua.deti.springcanteen.entities.Drink;
import pt.ua.deti.springcanteen.entities.Ingredient;
import pt.ua.deti.springcanteen.entities.MainDish;
import pt.ua.deti.springcanteen.entities.MainDishIngredients;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.entities.OrderMenu;
import pt.ua.deti.springcanteen.exceptions.InvalidOrderException;
import pt.ua.deti.springcanteen.service.impl.IPriceService;

class PriceServiceTest {
    PriceService priceService = new IPriceService(); 

    Menu availableMenu;
    OrderMenu requestedOrderMenu;
    Drink drinkOption1, drinkOption2;
    MainDish mainDishOption1, mainDishOption2;
    Ingredient ingredient1, ingredient2, ingredient3, ingredient4; 


    @BeforeEach
    void setup() {
        // available drink options
        drinkOption1 = new Drink();
        drinkOption1.setId(1L);
        drinkOption1.setName("Water");
        drinkOption1.setPrice(1.5f);
        drinkOption2 = new Drink();
        drinkOption2.setId(2L);
        drinkOption2.setName("Orange Juice");
        drinkOption2.setPrice(2.0f);

        // ingredients for mainDish1 (fat pancakes)
        ingredient1 = new Ingredient(1L, "Sugar", 0.5f, 100);
        ingredient2 = new Ingredient(2L, "Milk", 1.0f, 50);

        Set<MainDishIngredients> mainDish1Ingredients = new HashSet<>();
        mainDish1Ingredients.add(new MainDishIngredients(1L, mainDishOption1, ingredient1, 2));
        mainDish1Ingredients.add(new MainDishIngredients(2L, mainDishOption1, ingredient2, 1));

        mainDishOption1 = new MainDish(mainDish1Ingredients);
        mainDishOption1.setId(1L);
        mainDishOption1.setName("Fat Pancakes");
        mainDishOption1.setPrice(3.0f);


        // ingredients for mainDish2 (thin pancakes)
        ingredient3 = new Ingredient(3L, "Vegan sugar", 0.25f, 10);
        ingredient4 = new Ingredient(4L, "Vegan Milk", 0.5f, 50);

        Set<MainDishIngredients> mainDish2Ingredients = new HashSet<>();
        mainDish2Ingredients.add(new MainDishIngredients(3L, mainDishOption2, ingredient3, 1));
        mainDish2Ingredients.add(new MainDishIngredients(4L, mainDishOption2, ingredient4, 2));


        mainDishOption2 = new MainDish(mainDish2Ingredients);
        mainDishOption2.setId(2L);
        mainDishOption2.setName("Thin Pancakes");
        mainDishOption2.setPrice(1.8f);

        // available menu
        Set<Drink> drinkOptions = new HashSet<>();
        drinkOptions.add(drinkOption1);
        drinkOptions.add(drinkOption2);

        Set<MainDish> mainDishOptions = new HashSet<>();
        mainDishOptions.add(mainDishOption1);
        mainDishOptions.add(mainDishOption2);

        availableMenu = new Menu(1L, "Pancakes", null, null, drinkOptions, mainDishOptions);
    }


    @Test
    void whenGetMenuPrice_thenPriceIsCorrectlyCalculated() {
        // setup an order for a pancakes menu with the 'Orange Juice' drink (2€) and 'Fat Pancakes' main dish
        // with default quantities
        requestedOrderMenu = new OrderMenu(null, availableMenu, 
            "{customized_drink: {item_id: 2}, customized_main_dish: {item_id:1, customized_ingredients: [{ingredient_id: 1, quantity: 2}, {ingredient_id: 2, quantity: 1}]}}");

        float actualOrderPrice = priceService.getOrderMenuPrice(requestedOrderMenu);
        System.out.println("price of drinkoption1:" + drinkOption1.getPrice());
        System.out.println("price of meaindishopt1:" + mainDishOption1.getPrice());
        assertThat(actualOrderPrice, is(drinkOption2.getPrice() + mainDishOption1.getPrice()));
    }

    @Test
    void whenMenu_hasInvalidMainDish_thenThrowError() {
        // this order requests a main dish that does not exist for the requested menu
        requestedOrderMenu = new OrderMenu(null, availableMenu, 
            "{customized_drink: {item_id: 2}, customized_main_dish: {item_id:3, customized_ingredients: [{ingredient_id: 1, quantity: 2}]}}");

        Exception exceptionThrown = assertThrows(InvalidOrderException.class, () -> {
            priceService.getOrderMenuPrice(requestedOrderMenu);
        });

        assertThat(exceptionThrown.getMessage(), containsString("Provided main dish does not exist as an option in the given menu."));
    }

    @Test
    void whenMenu_hasInvalidDrink_thenThrowError() {
        // this order requests a drink that does not exist for the requested menu
        requestedOrderMenu = new OrderMenu(null, availableMenu, 
            "{customized_drink: {item_id: 3}, customized_main_dish: {item_id:1, customized_ingredients: [{ingredient_id: 1, quantity: 2}, {ingredient_id: 2, quantity: 1}]}}");

        Exception exceptionThrown = assertThrows(RuntimeException.class, () -> {
            priceService.getOrderMenuPrice(requestedOrderMenu);
        });

        assertThat(exceptionThrown.getMessage(), containsString("Provided drink does not exist as an option in the given menu."));

    }

    @Test
    void whenMenu_isMissingIngredientsForMainDish_thenThrowError() {
        // this order request is missing an ingredient in the customization string
        // which is not expected
        requestedOrderMenu = new OrderMenu(null, availableMenu, 
            "{customized_drink: {item_id: 2}, customized_main_dish: {item_id:1, customized_ingredients: [{ingredient_id: 1, quantity: 2}]}}");

        Exception exceptionThrown = assertThrows(RuntimeException.class, () -> {
            priceService.getOrderMenuPrice(requestedOrderMenu);
        });

        String expectedMessage = String.format("You must specify all ingredients in the main dish and their quantity when ordering a certain menu. Ingredient %d '%s' not found.", ingredient2.getId(), ingredient2.getName());
        assertThat(exceptionThrown.getMessage(), containsString(expectedMessage));

    }

    @Test
    void whenMenu_hasLessIngredientQuantityThanBase_thenPriceShouldNotBeLowerThanBasePrice() {
        // setup an order for a pancakes menu with the 'Orange Juice' drink (2€) and 'Fat Pancakes' main dish
        // the requested main dish costs 3€ base.
        // this order request removed all 'sugar' ingredient and the price should not be lowered when removing ingredients.
        requestedOrderMenu = new OrderMenu(null, availableMenu, 
            "{customized_drink: {item_id: 2}, customized_main_dish: {item_id:1, customized_ingredients: [{ingredient_id: 1, quantity: 1}, {ingredient_id: 2, quantity: 1}]}}");

        float actualOrderPrice = priceService.getOrderMenuPrice(requestedOrderMenu);

        assertThat(actualOrderPrice, is(drinkOption2.getPrice() + mainDishOption1.getPrice()));
    }

    @Test
    void whenMenu_hasMoreIngredientsThanBase_thenPriceShouldBeHigher() {
        // setup an order for a pancakes menu with the 'Water' drink (1.5€) and 'Thin Pancakes' main dish
        // with 9x extra 'vegan sugar' and 2x extra 'vegan milk'
        requestedOrderMenu = new OrderMenu(null, availableMenu, 
            "{customized_drink: {item_id: 1}, customized_main_dish: {item_id:2, customized_ingredients: [{ingredient_id: 3, quantity: 10}, {ingredient_id: 4, quantity: 4}]}}");

        float actualOrderPrice = priceService.getOrderMenuPrice(requestedOrderMenu);

        // price = drink_price + main_dish_base_price + extra ingredients price
        float expectedPrice = drinkOption1.getPrice() + mainDishOption2.getPrice() + ingredient3.getPrice() * 9 + ingredient4.getPrice() * 2;
        assertThat(actualOrderPrice, is(expectedPrice));
    }
}
