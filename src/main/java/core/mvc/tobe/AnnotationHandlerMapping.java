package core.mvc.tobe;

import com.google.common.collect.Maps;
import core.annotation.web.Controller;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;
import core.mvc.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotationHandlerMapping implements HandlerMapping {
    private static final int REQUEST_METHOD_ZERO = 0;

    private Object[] basePackage;

    private Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
    }

    public void initialize() {
        ControllerScanner controllerScanner = new ControllerScanner(this.basePackage);
        controllerScanner.getControllers().forEach(this::setHandlerExecutions);
    }

    private void setHandlerExecutions(Class<?> controllerClass, Object controllerInstance) {
        String controllerPath = controllerClass.getAnnotation(Controller.class).value();
        List<Method> methods = getRequestMappingMethods(controllerClass);
        methods.forEach(method -> setHandlerExecutionsPerMethod(controllerInstance, method, controllerPath));
    }

    private void setHandlerExecutionsPerMethod(Object controllerInstance, Method method, String controllerPath) {
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        RequestMethod[] requestMethods = requestMapping.method();
        if (requestMethods.length == REQUEST_METHOD_ZERO) {
            requestMethods = RequestMethod.values();
        }

        for (RequestMethod requestMethod : requestMethods) {
            handlerExecutions.put(new HandlerKey(controllerPath + requestMapping.value(), requestMethod), new HandlerExecution(controllerInstance, method));
        }
    }

    private List<Method> getRequestMappingMethods(Class<?> controllerClass) {
        return Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .collect(Collectors.toList());
    }

    public HandlerExecution getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod().toUpperCase());
        return handlerExecutions.get(new HandlerKey(requestUri, requestMethod));
    }
}
