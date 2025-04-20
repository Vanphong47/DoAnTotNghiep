package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.Brand;
import com.project.DuAnTotNghiep.entity.Material;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.service.BrandService;
import com.project.DuAnTotNghiep.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping("/material-all")
    public String getAllMaterial(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "sort", defaultValue = "name,asc") String sortField) {
        int pageSize = 5; // Number of items per page
        String[] sortParams = sortField.split(",");
        String sortFieldName = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(sortDirection, sortFieldName);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Material> materialPage = materialService.getAllMaterial(pageable);

        model.addAttribute("sortField", sortFieldName);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("items", materialPage);

        return "admin/material";
    }

    @GetMapping("/material-create")
    public String viewAddMaterial(Model model){
        Material material = new Material();
        model.addAttribute("action", "/admin/material-save");
        model.addAttribute("Material", material);
        return "admin/material-create";
    }

    @PostMapping("/material-save")
    public String addMaterial(Model model, @Validated @ModelAttribute("Material") Material material, RedirectAttributes redirectAttributes) {
        try {
            // Check if the material name already exists
            if (materialService.existsByName(material.getName())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên chất liệu '" + material.getName() + "' đã tồn tại");
                return "redirect:/admin/material-create";
            }

            materialService.createMaterial(material);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm chất liệu mới thành công");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/material-create";
        }
        return "redirect:/admin/material-all";
    }

    @PostMapping("/material-update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Validated @ModelAttribute("Material") Material material, RedirectAttributes redirectAttributes) {

        // Check if the material name already exists, excluding the current material being updated
        if (materialService.existsByNameAndIdNot(material.getName(), id)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên chất liệu '" + material.getName() + "' đã tồn tại");
            return "redirect:/admin/material-detail/" + id;
        }

        try {
            Material material1 = materialService.updateMaterial(material);
            redirectAttributes.addFlashAttribute("successMessage", "Chất liệu " + material1.getCode() + " đã được cập nhật thành công");
        } catch (NotFoundException e) {
            return "404";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/material-detail/" + id;
        }
        return "redirect:/admin/material-all";
    }

    @GetMapping("/material-detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<Material> optional = materialService.findById(id);
        if (optional.isPresent()) {
            Material material = optional.get();
            model.addAttribute("Material", material);
            model.addAttribute("action", "/admin/material-update/" + material.getId());
            return "admin/material-create";
        } else {
            return "404";
        }
    }

    @GetMapping("/material-delete/{id}")
    public String delete(@PathVariable("id") Long id, ModelMap modelMap){
        materialService.delete(id);
        return "redirect:/admin/material-all";
    }

    // AJAX endpoint to check for duplicate material name
    @GetMapping("/check-material-name")
    @ResponseBody
    public Map<String, Boolean> checkMaterialName(@RequestParam("name") String name,
                                                  @RequestParam(value = "id", required = false) Long id) {
        boolean exists;
        if (id != null) {
            // Check for duplicates excluding the current material being updated
            exists = materialService.existsByNameAndIdNot(name, id);
        } else {
            // Check for duplicates during create
            exists = materialService.existsByName(name);
        }
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", !exists); // returns true if name is not duplicated
        return response;
    }
}
