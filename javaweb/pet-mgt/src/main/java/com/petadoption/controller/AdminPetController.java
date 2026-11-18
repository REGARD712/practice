package com.petadoption.controller;

import com.petadoption.entity.Pet;
import com.petadoption.exception.BusinessException;
import com.petadoption.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 管理员维护宠物资料及图片的控制器。
 * 路由统一位于 /admin/pets，访问权限由 /admin/** 的管理员规则控制。
 */
@Controller
@RequestMapping("/admin/pets")
@RequiredArgsConstructor
public class AdminPetController {
    /** 封装宠物、品种、图片和删除约束等业务操作。 */
    private final PetService petService;

    /**
     * 展示后台全部宠物。
     *
     * @param model 向列表模板传递宠物数据
     * @return 宠物管理列表模板逻辑名
     */
    @GetMapping
    public String pets(Model model) {
        model.addAttribute("pets", petService.findAll());
        return "admin/pets/list";
    }

    /**
     * 打开新建宠物表单。
     *
     * @param model 向模板传递空宠物对象和品种选项
     * @return 宠物表单模板逻辑名
     */
    @GetMapping("/new")
    public String newPet(Model model) {
        // 提供空对象供表单字段绑定，新建时具体默认值由页面或服务逻辑处理。
        model.addAttribute("pet", new Pet());
        model.addAttribute("breeds", petService.findAllBreeds());
        return "admin/pets/form";
    }

    /**
     * 校验并创建宠物，同时接收多图片上传。
     *
     * @param pet 表单绑定并经过 Bean Validation 校验的宠物资料
     * @param bindingResult 保存字段校验和业务错误
     * @param imageFiles 可选的请求文件列表，创建时服务层要求至少一张有效图片
     * @param model 失败时重新渲染表单所需模型
     * @param redirectAttributes 成功后向列表页传递一次性提示
     * @return 失败返回表单，成功重定向到宠物列表
     */
    @PostMapping
    public String createPet(@Valid @ModelAttribute("pet") Pet pet,
                            BindingResult bindingResult,
                            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        // 字段校验失败时不写文件或数据库，直接保留输入并回显错误。
        if (bindingResult.hasErrors()) {
            model.addAttribute("breeds", petService.findAllBreeds());
            return "admin/pets/form";
        }
        try {
            // 服务层负责文件校验、数据库事务、封面设置和失败后的文件补偿删除。
            petService.create(pet, imageFiles);
        } catch (BusinessException ex) {
            // 文件格式、大小、必填图片或品种问题作为表单级错误展示。
            bindingResult.reject("pet.save.failed", ex.getMessage());
            model.addAttribute("breeds", petService.findAllBreeds());
            return "admin/pets/form";
        }
        // 重定向到列表，避免浏览器刷新重复提交创建请求。
        redirectAttributes.addFlashAttribute("success", "宠物创建成功");
        return "redirect:/admin/pets";
    }

    /**
     * 打开指定宠物的编辑表单。
     *
     * @param id 待编辑宠物编号
     * @param model 向模板传递现有宠物、图片和品种选项
     * @return 宠物编辑表单模板逻辑名
     */
    @GetMapping("/{id}/edit")
    public String editPet(@PathVariable Long id, Model model) {
        model.addAttribute("pet", petService.getById(id));
        model.addAttribute("breeds", petService.findAllBreeds());
        return "admin/pets/form";
    }

    /**
     * 校验并更新指定宠物，可追加新的图片。
     *
     * @param id 路径中的宠物编号，是本次更新的权威标识
     * @param pet 表单绑定并校验后的宠物资料
     * @param bindingResult 保存字段及业务错误
     * @param imageFiles 可选的新图片列表，未选择时不修改已有图片
     * @param model 失败时重新渲染表单所需模型
     * @param redirectAttributes 成功后传递一次性提示
     * @return 失败返回表单，成功重定向到宠物列表
     */
    @PostMapping("/{id}")
    public String updatePet(@PathVariable Long id,
                            @Valid @ModelAttribute("pet") Pet pet,
                            BindingResult bindingResult,
                            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        // 使用路径编号覆盖请求体编号，防止通过篡改表单修改其他宠物。
        pet.setId(id);
        if (bindingResult.hasErrors()) {
            // 领养状态不再随表单提交，回显时补回数据库中的真实状态，避免编辑页误显示为默认的待领养。
            pet.setStatus(petService.getById(id).getStatus());
            model.addAttribute("breeds", petService.findAllBreeds());
            return "admin/pets/form";
        }
        try {
            // 服务层校验品种，保存新文件，并在数据库失败时清理本次新增文件。
            petService.update(pet, imageFiles);
        } catch (BusinessException ex) {
            bindingResult.reject("pet.save.failed", ex.getMessage());
            // 同样补回真实状态，保证失败回显页面展示的领养状态与数据库一致。
            pet.setStatus(petService.getById(id).getStatus());
            model.addAttribute("breeds", petService.findAllBreeds());
            return "admin/pets/form";
        }
        redirectAttributes.addFlashAttribute("success", "宠物信息已更新");
        return "redirect:/admin/pets";
    }

    /**
     * 将宠物相册中的指定图片设为封面。
     *
     * @param id 宠物编号
     * @param imageId 图片编号
     * @param redirectAttributes 向编辑页传递操作结果
     * @return 始终重定向回该宠物的编辑页
     */
    @PostMapping("/{id}/images/{imageId}/cover")
    public String setCoverImage(@PathVariable Long id,
                                @PathVariable Long imageId,
                                RedirectAttributes redirectAttributes) {
        try {
            // 服务层校验图片归属并在同一事务内切换唯一封面。
            petService.setCoverImage(id, imageId);
            redirectAttributes.addFlashAttribute("success", "封面已更新");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/pets/" + id + "/edit";
    }

    /**
     * 删除宠物相册中的指定图片。
     *
     * @param id 宠物编号
     * @param imageId 图片编号
     * @param redirectAttributes 向编辑页传递操作结果
     * @return 始终重定向回该宠物的编辑页
     */
    @PostMapping("/{id}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long id,
                              @PathVariable Long imageId,
                              RedirectAttributes redirectAttributes) {
        try {
            // 服务层保留最后一张照片，删除封面时自动指定接替图片并在提交后清理文件。
            petService.deleteImage(id, imageId);
            redirectAttributes.addFlashAttribute("success", "照片已删除");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/pets/" + id + "/edit";
    }

    /**
     * 删除指定宠物及其图片记录。
     *
     * @param id 待删除宠物编号
     * @param redirectAttributes 向列表页传递一次性结果提示
     * @return 始终重定向到宠物列表
     */
    @PostMapping("/{id}/delete")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 服务层禁止删除已有领养申请的宠物，并在数据库删除后清理图片文件。
            petService.delete(id);
            redirectAttributes.addFlashAttribute("success", "宠物已删除");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/pets";
    }

    /**
     * 将已领养宠物重新开放为待领养。
     * 这是审核流程之外唯一的手动状态操作，服务层负责状态条件与待审核申请校验。
     *
     * @param id 待重新开放的宠物编号
     * @param redirectAttributes 向列表页传递一次性结果提示
     * @return 始终重定向到宠物列表
     */
    @PostMapping("/{id}/reopen")
    public String reopenPet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 服务层仅允许 ADOPTED → AVAILABLE，并确认没有待审核申请。
            String petName = petService.reopen(id);
            redirectAttributes.addFlashAttribute("success", "已将「" + petName + "」重新开放为待领养");
        } catch (BusinessException ex) {
            // 状态不符或仍有待审核申请时，把明确原因回显到列表页。
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/pets";
    }
}
