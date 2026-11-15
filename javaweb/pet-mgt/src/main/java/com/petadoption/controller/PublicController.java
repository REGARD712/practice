package com.petadoption.controller;

import com.petadoption.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 面向所有访问者的公开页面控制器。
 * 提供首页统计、宠物分页检索、宠物详情和权限不足页面，不执行任何数据修改操作。
 */
@Controller
@RequiredArgsConstructor
public class PublicController {
    /** 宠物列表每页固定展示的记录数。 */
    private static final int PAGE_SIZE = 9;

    /** 提供宠物查询、分页和统计数据。 */
    private final PetService petService;

    /**
     * 展示系统首页。
     *
     * @param model 向首页模板传递推荐宠物和统计数据的模型
     * @return 首页模板逻辑名
     */
    @GetMapping("/")
    public String index(Model model) {
        // 查询用于首页展示的推荐宠物。
        model.addAttribute("featuredPets", petService.findFeatured());
        // 统计当前仍可申请领养的宠物数量。
        model.addAttribute("availableCount", petService.countByStatus("AVAILABLE"));
        // 统计已经完成领养的宠物数量。
        model.addAttribute("adoptedCount", petService.countByStatus("ADOPTED"));
        // 统计宠物品种数量，用于首页概览。
        model.addAttribute("breedCount", petService.countBreeds());
        return "index";
    }

    /**
     * 按关键词、品种和页码查询公开宠物列表。
     *
     * @param keyword 可选搜索关键词，支持前后带空白的输入
     * @param breedId 可选品种编号，非正数视为未筛选
     * @param page 从 1 开始的请求页码，缺省为第 1 页
     * @param model 向列表模板传递查询结果及分页状态
     * @return 宠物列表模板逻辑名
     */
    @GetMapping("/pets")
    public String pets(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long breedId,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        // 统一关键词形式：空输入转为空串，其余输入去除首尾空白。
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        // 仅保留有效的正数品种编号，避免非法值进入查询条件。
        Long normalizedBreedId = breedId != null && breedId > 0 ? breedId : null;
        // 先统计符合条件的总数，为分页边界计算提供依据。
        long total = petService.countSearch(normalizedKeyword, normalizedBreedId);
        // 采用向上取整计算总页数；即使没有结果也保留一个空页，便于模板渲染。
        int totalPages = Math.max(1, (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE));
        // 将请求页码限制在合法范围内，防止负数偏移或超出末页。
        int currentPage = Math.min(Math.max(page, 1), totalPages);

        // 根据归零后的页码计算数据库偏移量，并查询当前页宠物。
        model.addAttribute("pets", petService.findPage(
                normalizedKeyword, normalizedBreedId, (currentPage - 1) * PAGE_SIZE, PAGE_SIZE));
        // 提供品种筛选项。
        model.addAttribute("breeds", petService.findAllBreeds());
        // 回显规范化后的关键词，保持搜索框内容一致。
        model.addAttribute("keyword", normalizedKeyword);
        // 回显实际生效的品种筛选条件。
        model.addAttribute("breedId", normalizedBreedId);
        // 提供经过边界修正的当前页码。
        model.addAttribute("currentPage", currentPage);
        // 提供总页数，供分页导航判断是否显示前后页。
        model.addAttribute("totalPages", totalPages);
        return "pets/list";
    }

    /**
     * 展示指定宠物的公开详情。
     *
     * @param id 路径中的宠物编号
     * @param model 向详情模板传递完整宠物信息
     * @return 宠物详情模板逻辑名；不存在时由服务层抛出资源异常
     */
    @GetMapping("/pets/{id}")
    public String petDetail(@PathVariable Long id, Model model) {
        // 服务层负责加载宠物及图片，并处理宠物不存在的情况。
        model.addAttribute("pet", petService.getById(id));
        return "pets/detail";
    }

    /**
     * 展示权限不足页面，供 SecurityConfig 的拒绝访问处理器跳转。
     *
     * @return 403 模板逻辑名
     */
    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }
}
