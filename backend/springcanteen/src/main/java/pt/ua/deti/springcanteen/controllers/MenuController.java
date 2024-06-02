package pt.ua.deti.springcanteen.controllers;

import java.util.List;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import pt.ua.deti.springcanteen.dto.MenuResponseDTO;
import pt.ua.deti.springcanteen.service.MenuService;

@RestController
@RequestMapping("/api/menus")
@AllArgsConstructor
public class MenuController {
  private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
  private MenuService menuService;

  @Operation(summary = "List all available menus")
  @ApiResponse(
      responseCode = "200",
      description = "List of all available menus, including main dish options and their ingredients and drink options.",
      content = {
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = MenuResponseDTO.class))
      })
  @GetMapping("")
  public ResponseEntity<List<MenuResponseDTO>> getMenus() {
    logger.info("GET /api/menus - all menus");
    return ResponseEntity.ok(menuService.getAvailableMenus());
  }
}
