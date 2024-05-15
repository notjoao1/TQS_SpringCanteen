package pt.ua.deti.springcanteen.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import pt.ua.deti.springcanteen.controllers.MenuController;
import pt.ua.deti.springcanteen.dto.mappers.MenuResponseDTOMapper;
import pt.ua.deti.springcanteen.entities.Drink;
import pt.ua.deti.springcanteen.entities.Ingredient;
import pt.ua.deti.springcanteen.entities.MainDish;
import pt.ua.deti.springcanteen.entities.MainDishIngredients;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.service.impl.IMenuService;

@WebMvcTest(MenuController.class)
class MenuControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private IMenuService menuService;

    private MenuResponseDTOMapper mapper = new MenuResponseDTOMapper();

    Menu menu1, menu2;
    Drink drink1, drink2, drink3;
    MainDish mainDish1, mainDish2;
    Ingredient ingredient1, ingredient2;

    @BeforeEach
    void setup() {
        ingredient1 = new Ingredient();
        ingredient1.setId(1L);
        ingredient1.setName("Rice");
        ingredient1.setPrice(0.5f);

        ingredient2 = new Ingredient();
        ingredient2.setId(2L);
        ingredient2.setName("Beef");
        ingredient2.setPrice(0.5f);

        drink1 = new Drink();
        drink1.setId(1L);
        drink1.setName("Drink 1");
        drink1.setPrice(1.0f);

        drink2 = new Drink();
        drink2.setId(2L);
        drink2.setName("Drink 2");
        drink2.setPrice(2.0f);

        drink3 = new Drink();
        drink3.setId(3L);
        drink3.setName("Drink 3");
        drink3.setPrice(3.0f);

        menu1 = new Menu();
        menu1.setId(1L);
        menu1.setName("Menu 1");
        menu1.setPrice(1.0f);

        menu2 = new Menu();
        menu2.setId(2L);
        menu2.setName("Menu 2");
        menu2.setPrice(2.0f);

        Set<Drink> drinkMenu1 = new HashSet<>();
        drinkMenu1.add(drink1);
        drinkMenu1.add(drink2);

        Set<Drink> drinkMenu2 = new HashSet<>();
        drinkMenu2.add(drink1);
        drinkMenu2.add(drink3);

        menu1.setDrinkOptions(drinkMenu1);
        menu2.setDrinkOptions(drinkMenu2);

        mainDish1 = new MainDish();
        mainDish1.setId(1L);
        mainDish1.setName("Rice with Beef");
        mainDish1.setPrice(1.0f);

        mainDish2 = new MainDish();
        mainDish2.setId(2L);
        mainDish2.setName("Beefy rice");
        mainDish2.setPrice(2.0f);

        Set<MainDishIngredients> mainDishIngredients1 = new HashSet<>();
        mainDishIngredients1.add(new MainDishIngredients(1L, mainDish1, ingredient1, 2));

        Set<MainDishIngredients> mainDishIngredients2 = new HashSet<>();
        mainDishIngredients2.add(new MainDishIngredients(2L, mainDish2, ingredient1, 3));
        mainDishIngredients2.add(new MainDishIngredients(3L, mainDish2, ingredient2, 1));

        mainDish1.setMainDishIngredients(mainDishIngredients1);
        mainDish2.setMainDishIngredients(mainDishIngredients2);

        Set<MainDish> mainDishMenu1 = new HashSet<>();
        mainDishMenu1.add(mainDish1);
        mainDishMenu1.add(mainDish2);

        Set<MainDish> mainDishMenu2 = new HashSet<>();
        mainDishMenu2.add(mainDish1);
        mainDishMenu2.add(mainDish2);

        menu1.setMainDishOptions(mainDishMenu1);
        menu2.setMainDishOptions(mainDishMenu2);

    }

    @Test
    void givenListOfMenus_whenGetAllMenus_returnsMenusWithAllInformation() {
        when(menuService.getAvailableMenus()).thenReturn(
                Arrays.asList(mapper.toDTO(menu1), mapper.toDTO(menu2)));

        RestAssuredMockMvc.
            given().
                mockMvc(mockMvc).
            when().
                get("api/menus").
            then().
                statusCode(HttpStatus.SC_OK).
                body("size()", is(2)).
                body("[0].drinkOptions.name", hasItems(drink1.getName(), drink2.getName())).
                body("[1].drinkOptions.name", hasItems(drink1.getName(), drink3.getName())).
                body("[0].mainDishOptions.mainDishIngredients.quantity", notNullValue()); // check if ingredients was loaded
    }
}
