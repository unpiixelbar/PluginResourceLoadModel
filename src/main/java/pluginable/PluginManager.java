package pluginable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginManager {

    private final List<PluginInterface> loadedplugins = new ArrayList<>();

    public PluginManager(String path) {
        File pluginsDir = new File(path, "plugins");

        if (pluginsDir.exists()) {
            //pluginsfolder exists
            File[] files = pluginsDir.listFiles();
            if (files != null) {
                for (File f : files)
                    if (!f.isDirectory()) {
                        loadPlugin(f);
                    }
            }
        } else {
            //pluginsfolder does not exist
            if (pluginsDir.mkdir()) {
                System.out.println("Dictionary created: " + pluginsDir.getPath());
            }
        }
    }


    private void loadPlugin(File file) {

        URL urlFile;
        //trying to load file, convert it first to URI and then to URL
        try {
            urlFile = file.toURI().toURL();
        } catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            return;
        }

        //trying to create JAR-file from file
        try (
                //use JarFIle and URLClassLoader as auto-closable
                JarFile jarFile = new JarFile(file);
                //use classloader of this class as parent classLoader
                URLClassLoader classLoader = new URLClassLoader(new URL[]{urlFile}, this.getClass().getClassLoader())
        ) {

            //load manifest
            Manifest manifest = jarFile.getManifest();
            //read attributes from manifest
            Attributes attributes = manifest.getMainAttributes();
            //get main class from attributes
            String main = attributes.getValue(Attributes.Name.MAIN_CLASS);
            if (main == null) {
                System.err.println(file.getName() + " has no main specified");
                return;
            }

            String title = attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
            if (title == null) {
                //https://maven.apache.org/shared/maven-archiver/index.html
                System.err.println(file.getName() + " has no implementation title specified");
                return;
            }

            //https://javapapers.com/core-java/java-class-loader/

            //load class with classLoader of jarFile
            Class<?> cl = classLoader.loadClass(main);

            Class<?>[] classes = cl.getClasses();

            //get implemented interfaces of class
            Class<?>[] interfaces = cl.getInterfaces();
            //iterate over interfaces and check for PluginInterface.class
            boolean isPlugin = false;
            for (Class<?> anInterface : interfaces) {
                if (anInterface.equals(PluginInterface.class)) {
                    isPlugin = true;
                    break;
                }
            }

            if (isPlugin) {
                //load all classes in jar file
                loadClassesOfjarFile(jarFile, cl.getClassLoader());

                //add the pluginfile
                PluginInterface plugin = (PluginInterface) cl.getConstructor().newInstance();

                loadedplugins.add(plugin);
            }
        } catch (Exception e) {
            System.err.println("Error on checking " + file.getName() + " for plugin");
            e.printStackTrace();
        }
    }

    public List<PluginInterface> getLoadedplugins() {
        return loadedplugins;
    }

    public static void loadClassesOfjarFile(JarFile jarFile, ClassLoader classLoader) {
        jarFile.entries().asIterator().forEachRemaining(jarEntry -> {
            String jarEntryName = jarEntry.getName();
            if ((jarEntryName.endsWith(".class"))) {
                String className = jarEntry.getName().replaceAll("/", "\\.");
                String myClass = className.substring(0, className.lastIndexOf('.'));

                try {
                    //System.out.println("Trying to load " + myClass + " from " + jarFile.getName());
                    Class<?> clazz = classLoader.loadClass(myClass);
                    /*
                    if (clazz != null) {
                        System.out.println("        loaded " + clazz.getName());
                    }//*/
                } catch (ClassNotFoundException e) {
                    System.err.println(e);
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            } else if (jarEntryName.endsWith(".xml")) {
                String resourceName = jarEntry.getName().replaceAll("/", "\\.");
                classLoader.getResourceAsStream(jarEntry.getName());
//                System.out.println("loaded "+ resource.toString());
            } else if (jarEntryName.endsWith(".properties")) {
                String resourceName = jarEntry.getName().replaceAll("/", "\\.");
                InputStream resourceAsStream = classLoader.getResourceAsStream(jarEntry.getName());
                try {
                    new Properties().load(resourceAsStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                System.out.println("loaded "+ resource.toString());
            }
        });
    }

}
