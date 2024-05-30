package pt.ua.deti.springcanteen.dto.mappers;

import org.springframework.stereotype.Component;

import pt.ua.deti.springcanteen.dto.MenuResponseDTO;
import pt.ua.deti.springcanteen.entities.Menu;

@Component
public class MenuResponseDTOMapper {
  public MenuResponseDTO toDTO(Menu menu) {
    var menuDTO = new MenuResponseDTO();
    menuDTO.setId(menu.getId());
    menuDTO.setName(menu.getName());
    menuDTO.setImageLink(menu.getImageLink());
    menuDTO.setDrinkOptions(menu.getDrinkOptions());
    menuDTO.setMainDishOptions(menu.getMainDishOptions());
    return menuDTO;
  }
}
