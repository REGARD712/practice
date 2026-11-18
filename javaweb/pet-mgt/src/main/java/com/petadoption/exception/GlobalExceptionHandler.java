package com.petadoption.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring MVC 全局异常处理器，集中转换指定异常，避免各控制器重复编写异常处理逻辑。
 */
// 使本类中的 @ExceptionHandler 方法对全局控制器生效。
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理上传大小超限异常，并通过重定向返回合适的页面。
     * 编辑宠物失败时回到原编辑页，新增宠物失败时回到新增页，其他来源回到首页。
     */
    // 仅捕获 Spring 在请求大小超过配置上限时抛出的 MaxUploadSizeExceededException。
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleUploadLimit(MaxUploadSizeExceededException ex,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        // flash 属性只在重定向后的下一次请求中保留，用于在目标页面展示错误提示。
        redirectAttributes.addFlashAttribute("error", "上传文件超出限制：单张图片不能超过2MB，5 张合计不能超过10MB");
        // 读取当前请求路径，据此判断用户原来所在的新增或编辑场景。
        String requestUri = request.getRequestURI();
        // 形如 /admin/pets/{数字ID} 的路径表示编辑已有宠物。
        if (requestUri.matches(".*/admin/pets/[0-9]+")) {
            // 截取路径最后一个斜杠后的数字部分作为宠物 ID。
            String id = requestUri.substring(requestUri.lastIndexOf('/') + 1);
            // 重定向到该宠物的编辑页，避免停留在上传失败的请求上。
            return "redirect:/admin/pets/" + id + "/edit";
        }
        // 请求以 /admin/pets 结尾时表示新增宠物提交失败。
        if (requestUri.endsWith("/admin/pets")) {
            return "redirect:/admin/pets/new";
        }
        // 其他上传入口统一回到站点首页。
        return "redirect:/";
    }

    /**
     * 处理未被控制器就地捕获的业务异常。
     * 统一渲染带业务提示的错误页并返回 400，避免异常直接落到 500 错误页；
     * 例如会话账号已被删除或停用时，会在此展示“请重新登录”的明确提示。
     */
    // 捕获所有未被具体控制器 try-catch 的 BusinessException。
    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException ex) {
        // 复用通用错误视图，保持页面风格与其它失败页一致。
        ModelAndView modelAndView = new ModelAndView("error");
        // 业务规则不满足属于可纠正的客户端错误，使用 400 而不是 500。
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        // 将异常中的可读原因交给 error 属性，供页面直接展示。
        modelAndView.addObject("error", ex.getMessage());
        return modelAndView;
    }

    /** 将资源不存在异常渲染为 404 视图，并返回正确的 HTTP 状态码。 */
    // 优先于 @ResponseStatus 的默认处理，使用自定义 404 页面展示异常信息。
    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        // 创建模型视图对象，并指定视图名为 404。
        ModelAndView modelAndView = new ModelAndView("404");
        // 显式设置 HTTP 404，确保浏览器和客户端能识别资源不存在。
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        // 将异常消息放入模型，供 404 模板以 error 名称读取。
        modelAndView.addObject("error", ex.getMessage());
        // 返回包含状态、视图和错误消息的完整处理结果。
        return modelAndView;
    }
}
