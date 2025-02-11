package gg.umu.inventoryApi;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import gg.umu.inventoryApi.ymls.GuiYML;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PageFactory {

    private PageFactory(){
        
    }
    /**
     * 
     * @param <T>
     * @param pageClass
     * @param parameters First the Owner(SuperPlayer)
     * @return
     */
    public static <T extends BasePage> T createPage(Class<T> pageClass, List<Object> parameters) {
        GuiYML guiYML = getGuiYML(pageClass);
        if (guiYML == null) {
            return null;
        }
        List<Object> parametersList = new LinkedList<>();
        parametersList.addAll(parameters);
        parametersList.addFirst(guiYML);
        Class[] parameterTypes = parametersList.stream().map(Object::getClass).toArray(Class[]::new);
        try {
            return pageClass.getConstructor(parameterTypes).newInstance(parametersList.toArray(Object[]::new));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private static GuiYML getGuiYML(Class<? extends BasePage> pageClass) {
        String[] folder = pageClass.getPackageName().split("\\.");
        int index = Arrays.asList(folder).indexOf("gui");
        String filename = "/gui/"  + folder[index + 1] + "/" + pageClass.getSimpleName() +".yml";
        InputStream inputStream = pageClass.getClass().getResourceAsStream(filename);
        if (inputStream == null) {
            log.error("GuiYML not Found " + filename);
            return null;
        }
        Yaml yaml = new Yaml(new Constructor(GuiYML.class, new LoaderOptions()));
        return yaml.load(inputStream);
        
    }
}