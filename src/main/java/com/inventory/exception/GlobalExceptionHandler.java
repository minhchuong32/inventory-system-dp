package com.inventory.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, RedirectAttributes ra) {
        log.warn("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }

    // KHÔNG khai báo @ExceptionHandler(Exception.class) vì nó sẽ bắt luôn
    // AccessDeniedException và nuốt mất trước khi Spring Security xử lý.
    // Spring Security's ExceptionTranslationFilter nằm trong filter chain,
    // TRƯỚC DispatcherServlet — nên nếu @ControllerAdvice bắt AccessDeniedException
    // thì exception không bao giờ thoát ra được filter chain để redirect /access-denied.
    //
    // Giải pháp: chỉ bắt các RuntimeException cụ thể, KHÔNG bắt Exception.class chung.
    @ExceptionHandler({
        RuntimeException.class,
        IllegalArgumentException.class,
        IllegalStateException.class
    })
    public String handleRuntime(RuntimeException ex, RedirectAttributes ra) {
        // Nếu là AccessDeniedException (subclass của RuntimeException) → ném lại
        // để Spring Security filter xử lý redirect sang /access-denied
        if (ex instanceof AccessDeniedException) {
            throw ex;
        }
        log.error("Runtime exception: {}", ex.getMessage());
        ra.addFlashAttribute("error", "Đã xảy ra lỗi: " + ex.getMessage());
        return "redirect:/dashboard";
    }
}