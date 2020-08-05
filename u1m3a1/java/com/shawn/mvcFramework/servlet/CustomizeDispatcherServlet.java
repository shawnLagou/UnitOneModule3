package com.shawn.mvcFramework.servlet;

import com.shawn.mvcFramework.annotations.*;
import com.shawn.mvcFramework.pojo.Handler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomizeDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    // 缓存扫描的类的全限定类名
    private List<String> classNames = new ArrayList<>();

    // ioc容器
    private Map<String,Object> ioc = new HashMap<>();

    // 存储URL和method的映射关系
    //    private Map<String,Method> handlerMapping = new HashMap<>();
    private List<Handler> handlerMapping = new ArrayList<>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // 1 加载配置文件 springmvc.properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);
        // 2扫描相关注解、扫描相关类
        doScan(properties.getProperty("scanPackage"));
        // 3 初始化bean对象（实现ioc容器，基于注解）
        doInstance();
        // 4 实现依赖注入
        doAutoWired();
        // 5 构造一个HandlerMapping处理器映射器，将配置好的url和method建立映射关系
        initHandlerMapping();
        System.out.println("mvc 初始化完成......");
        // 等待请求进入，处理请求
    }

    // 构造handlerMapping处理映射器
    // 将URL和handler进行关联
    private void initHandlerMapping() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String,Object> entry: ioc.entrySet()) {
            // 获取ioc中当前遍历的对象的class类型
            Class<?> clazz = entry.getValue().getClass();
            List<String> controllerSecurity = new ArrayList<>();
            if (!clazz.isAnnotationPresent(Controller.class)) continue;

            String baseUrl = "";
            String[] securityUserList;
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                baseUrl = annotation.value(); // 等同于 /demo
            }

            if (clazz.isAnnotationPresent(Security.class)) {
                Security annotation = clazz.getAnnotation(Security.class);
                securityUserList = annotation.httpMethodConstraints();
                if (securityUserList != null) {
                    for (String user : securityUserList) {
                        controllerSecurity.add(user);
                    }
                }
            }

            // 获取方法
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                List<String> methodSecurity = new ArrayList<>();
                methodSecurity.addAll(controllerSecurity);
                // 方法没有RequestMapping就不处理
                if (!method.isAnnotationPresent(RequestMapping.class) && !method.isAnnotationPresent(Security.class)) continue;

                // 如果标识就处理
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                String methodUrl = annotation.value(); // /query
                String url = baseUrl + methodUrl;

                Security securityAnnotation = method.getAnnotation(Security.class);
                if (securityAnnotation != null) {
                    securityUserList = securityAnnotation.httpMethodConstraints();
                    if (securityUserList != null) {
                        for (String user : securityUserList) {
                            if (!methodSecurity.contains(user))
                                methodSecurity.add(user);
                        }
                    }
                }


                // 把method所有信息及URL封装为一个handler对象
                Handler handler = new Handler(entry.getValue(), method, Pattern.compile(url), methodSecurity);

                // 计算方法的参数位置信息  query(HttpServletRequest request, HttpServletResponse response, String name)
                Parameter[] parameters = method.getParameters();
                for (int j = 0; j < parameters.length; j++) {
                    Parameter parameter = parameters[j];

                    if (parameter.getType() == HttpServletRequest.class || parameter.getType() == HttpServletResponse.class) {
                        handler.getParamIndexMapping().put(parameter.getType().getSimpleName(), j);
                    } else {
                        handler.getParamIndexMapping().put(parameter.getName(), j);
                    }
                }
//                handlerMapping.put(url, method);
                handlerMapping.add(handler);


            }
        }
    }

    // 实现依赖注入
    private void doAutoWired() {
        if (ioc.isEmpty()) return;

        // 有对象在进行依赖注入处理

        // 遍历ioc中的所有对象，查看对象中的字段，是否有@Autowired注解，如果有需要维护依赖注入关系
        for (Map.Entry<String,Object> entry: ioc.entrySet()) {
            // 获取bean对象中的字段信息
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            // 遍历判断处理
            for (int i = 0; i < declaredFields.length; i++) {
                Field declaredField = declaredFields[i];
                if (!declaredField.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                // 如果有该注解
                Autowired annotation = declaredField.getAnnotation(Autowired.class);
                String beanName = annotation.value(); // 需要注入的bean ID
                if ("".equals(beanName.trim())) {
                    // 没有配置具体的bean id就需要根据当前字段类型注入
                    beanName = declaredField.getType().getName();
                }

                // 开启赋值
                declaredField.setAccessible(true);

                try {
                    declaredField.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ioc容器
    // 基于classNames缓存的类的全限定类名，以及反射技术，完成对象创建和管理
    private void doInstance(){
        if (classNames.size() == 0) return;

        try {
            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i); // com.shawn.demo.controller.DemoController

                // 反射
                Class<?> clazz = Class.forName(className);
                // 区分controller，区分service
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // controller的id此处不做过多处理，不取value，就拿类的首字母小写作为id，保存到ico中
                    String simpleName = clazz.getSimpleName(); // DemoController
                    String lowerFirstSimpleName = lowerFirst(simpleName);  // demoController
                    Object o = clazz.newInstance();
                    ioc.put(lowerFirstSimpleName, o);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service annotation = clazz.getAnnotation(Service.class);
                    // 获取注解value值
                    String beanName = annotation.value();

                    // 如果指定了id，就以指定的为准
                    if (!"".equals(beanName.trim())) {
                        ioc.put(beanName, clazz.newInstance());
                    } else {
                        // 如果没有指定，以类名首字母小写
                        beanName = lowerFirst(clazz.getSimpleName());
                        ioc.put(beanName, clazz.newInstance());
                    }

                    // service层往往是有接口的，面向接口开发，此时再以接口名为id，放入一份对象到ioc中，编译后期根绝接口类型注入
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> anInterface = interfaces[j];
                        // 以接口的全限定类名作为id放入
                        ioc.put(anInterface.getName(), clazz.newInstance());
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        if ('A' <= chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    // 扫描类
    // scanPackage: com.shawn
    private void doScan(String scanPackage) {
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "/");
        try {
            scanPackagePath = URLDecoder.decode(scanPackagePath, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        File pack = new File(scanPackagePath);
        if (pack.exists()) {
            File[] files = pack.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) { // 子package
                        // 递归
                        doScan(scanPackage + "." + file.getName()); // com.shawn.demo.controller
                    } else if (file.getName().endsWith(".class")) {
                        String className = scanPackage + "." + file.getName().replaceAll(".class", "");
                        classNames.add(className);
                    }
                }
            }
        }

    }

    // 加载配置文件
    private void doLoadConfig(String contextConfigLocation){
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 接收处理请求:根据URL，找到对应的Method方法，进行调用
        // 获取URI
//        String requestUri = req.getRequestURI();
//        Method method = handlerMapping.get(requestUri); // 获取到一个放射的方法
        // 反射调用，需要传入对象，需要传入参数
//        method.invoke()

        // 根据URI获取到能够处理当前请求的handler （从handlermapping中）
        Handler handler = getHandler(req);

        if (handler == null) {
            resp.getWriter().write("404 Not Found");
            return;
        }



        // 参数绑定
        // 获取所有参数类型数组，这个数组的长度就是我们最后要传入的args数组的长度
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();

        // 根据上述数组长度创建一个新的数组（参数数组，是要传入反射调用的）
        Object[] paraValues = new Object[parameterTypes.length];

        // 以下就是为了向参数数组中塞值，而且还得保证参数的顺序和方法中形参顺序一致

        Map<String, String[]> parameterMap = req.getParameterMap();

        // 遍历request中所有参数  （填充除了request，response之外的参数）
        for(Map.Entry<String,String[]> param: parameterMap.entrySet()) {
            // name=1&name=2   name [1,2]
            String value = StringUtils.join(param.getValue(), ",");  // 如同 1,2

            if (handler.getSecurity() != null && !handler.getSecurity().contains(value)) {
                resp.getWriter().write("Limited user");
                return;
            }

            // 如果参数和方法中的参数匹配上了，填充数据
            if (!handler.getParamIndexMapping().containsKey(param.getKey())) {
                continue;
            }

            // 方法形参确实有该参数，找到它的索引位置，对应的把参数值放入paraValues
            Integer index = handler.getParamIndexMapping().get(param.getKey());//name在第 2 个位置
            paraValues[index] = value;  // 把前台传递过来的参数值填充到对应的位置去
        }
        int requestIndex = handler.getParamIndexMapping().get(HttpServletRequest.class.getSimpleName()); // 0
        paraValues[requestIndex] = req;

        int responseIndex = handler.getParamIndexMapping().get(HttpServletResponse.class.getSimpleName()); // 1
        paraValues[responseIndex] = resp;

        // 最终调用handler的method属性
        try {
            handler.getMethod().invoke(handler.getController(),paraValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) return null;

        String url = req.getRequestURI();

        for(Handler handler: handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) continue;
            return handler;
        }
        return null;
    }


}
