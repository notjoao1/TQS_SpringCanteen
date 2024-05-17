package pt.ua.deti.springcanteen.service;
import java.util.List;
import java.util.Optional;

import pt.ua.deti.springcanteen.dto.MenuResponseDTO;
import pt.ua.deti.springcanteen.entities.Menu;

public interface MenuService {
    List<MenuResponseDTO> getAvailableMenus();

    Optional<Menu> getMenuById(Long menuId);
}
