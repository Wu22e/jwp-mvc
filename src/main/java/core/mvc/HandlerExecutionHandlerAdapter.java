package core.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HandlerExecutionHandlerAdapter implements HandlerAdapter {
    @Override
    public boolean supports(Object handler) {
        return false;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return null;
    }
}
