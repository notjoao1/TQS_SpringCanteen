package pt.ua.deti.springcanteen.controllers;

import java.util.List;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pt.ua.deti.springcanteen.dto.MenuClientResponseDTO;
import pt.ua.deti.springcanteen.dto.MenuResponseDTO;
import pt.ua.deti.springcanteen.service.MenuService;

@RestController
@RequestMapping("/api/menus")
@AllArgsConstructor
public class MenuController {
    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
    private MenuService menuService;

    @GetMapping("")
    public ResponseEntity<List<MenuResponseDTO>> getMenus() {
        logger.info("GET /api/menus - all menus");
        return ResponseEntity.ok(menuService.getAvailableMenus());
    }
}
