package pt.ua.deti.springcanteen.service;
import java.util.List;

import pt.ua.deti.springcanteen.dto.MenuResponseDTO;

public interface MenuService {
    List<MenuResponseDTO> getAvailableMenus();
}
