package pt.ua.deti.springcanteen.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pt.ua.deti.springcanteen.dto.MenuResponseDTO;
import pt.ua.deti.springcanteen.entities.Menu;
import pt.ua.deti.springcanteen.repositories.MenuRepository;
import pt.ua.deti.springcanteen.service.MenuService;

@Service
public class IMenuService implements MenuService {
    private static final Logger logger = LoggerFactory.getLogger(IMenuService.class);
    private MenuRepository menuRepository;

    @Autowired
    public IMenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public List<MenuResponseDTO> getAvailableMenus() {
        logger.info("Getting all menus...");
        List<Menu> menusDb = menuRepository.findAll();
        
        return menusDb.stream().map(menu -> {
            var menuDTO = new MenuResponseDTO();
            menuDTO.setId(menu.getId());
            menuDTO.setName(menu.getName());
            menuDTO.setPrice(menu.getPrice());
            menuDTO.setDrinkOptions(menu.getDrinkOptions());
            menuDTO.setMainDishOptions(menu.getMainDishOptions());
            return menuDTO;
        }).toList();
    }


}
