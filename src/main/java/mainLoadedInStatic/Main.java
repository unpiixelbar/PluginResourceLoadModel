package mainLoadedInStatic;

import pluginable.PluginInterface;
import pluginable.PluginManager;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.Locale;

public class Main {
    public static final String DEFAULT_PATH =  FileSystems.getDefault().getPath("").toAbsolutePath().toString();
    public static void main(String[] args) {
        PluginManager plugins = new PluginManager(DEFAULT_PATH);

        List<PluginInterface> loadedPlugins = plugins.getLoadedplugins();

        for (PluginInterface loadedPlugin : loadedPlugins) {
            loadedPlugin.loadResourceBundle(Locale.ENGLISH);
        }
    }
}
