package httpserver.utils;

import httpserver.server.Service;
import seb.dal.ServiceNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Router {

    private Map<String, Service> serviceRegistry = new HashMap<>();

    public void addService(String route, Service service)
    {
        this.serviceRegistry.put(route, service);
    }

    public void removeService(String route)
    {
        this.serviceRegistry.remove(route);
    }

    public Service resolve(String route)
    {
        Service service = this.serviceRegistry.get(route);
        if (service == null) {
            throw new ServiceNotFoundException("Service not found for route: " + route);
        }
        return service;
    }

    /*private Map<Pattern, Service> serviceRegistry = new HashMap<>();

    public void addService(String route, Service service) {

        if (route.contains("{") && route.contains("}")) {
            // Convert route to a regex Pattern
            String routePattern = route.replaceAll("\\{\\w+", "(\\\\w+)");
            this.serviceRegistry.put(Pattern.compile(routePattern), service);
        } else {
            // It's a fixed route, so add it directly without conversion
            this.serviceRegistry.put(Pattern.compile(Pattern.quote(route)), service);
        }

    }

    public Service resolve(String path) throws ServiceNotFoundException {

        Service fixedRouteService = serviceRegistry.get(path);
        System.out.println(path);
        if (fixedRouteService != null) {
            return fixedRouteService;
        }
        for (Map.Entry<Pattern, Service> entry : serviceRegistry.entrySet()) {
            Matcher matcher = entry.getKey().matcher(path);
            if (matcher.matches()) {
                // Found a matching service
                // Extract  path parameters
                if (matcher.groupCount() >= 1) {
                    String username = matcher.group(1);
                    List<String> pathParts = new ArrayList<>();
                    pathParts.add(username);
                }
                 // Assuming the first group is the username
                // You may pass it to the service here, or store it in a context object
                return entry.getValue();
            }
        }
        throw new ServiceNotFoundException("Service not found for route: " + path);
    }*/

}
