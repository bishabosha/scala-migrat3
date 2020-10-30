package interfaces;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import coursierapi.Module;
import coursierapi.*;

public interface Migrate {
    String migrateVersion = "0.1.0-SNAPSHOT";
    ScalaVersion scalaVersion = ScalaVersion.of("2.13");
    
    static Migrate fetchAndClassloadInstance() throws Exception {
        List<URL> jars = getJars();
        
        ClassLoader classLoader = new URLClassLoader(jars.stream().toArray(URL[]::new), Migrate.class.getClassLoader());
        Class<?> cls = classLoader.loadClass("migrate.internal.implementations.MigrateImpl");
        Constructor<?> ctor = cls.getDeclaredConstructor();
        ctor.setAccessible(true);
        return (Migrate) ctor.newInstance();
        
    }
    
    MigrateService getService();

    // put all needed dependecies here.
    private static List<URL> getJars() throws Exception {
        Dependency migrate = Dependency.parse(
                "ch.epfl.scala:::migrate:" + migrateVersion,
                scalaVersion
        );
        return fetch(Collections.singletonList(migrate), ResolutionParams.create());
    }
    

    private static List<URL> fetch(List<Dependency> dependencies, ResolutionParams resolutionParams) throws Exception {
        List<URL> jars = new ArrayList<>();
            List<File> files = Fetch.create()
                    .withDependencies(dependencies.stream().toArray(Dependency[]::new))
                    .withResolutionParams(resolutionParams)
                    .fetch();
            for (File file : files) {
                URL url = file.toURI().toURL();
                jars.add(url);
            }
        return jars;
    }
}
