package fr.kazejiyu.playfx.injection.internal;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.kazejiyu.playfx.Play;
import fr.kazejiyu.playfx.configuration.SerializedProperties;
import javafx.util.Callback;

/**
 * A custom controller factory that handles dependency injection. <br>
 * <br>
 * Instances of this class are supposed to be given to {@FXMLLoader.setControllerFactory} method.
 * 
 * @author Emmanuel CHEBBI
 */
public class InjectedControllerFactory implements Callback<Class<?>, Object> {
	
	private static final String CONFIG_FILE = "config.properties"; 
	
	private final Injector injector;
	/** Creates instances upon fields' name */
	private final Function<String, Object> instanciator;
	
	private static final Logger LOGGER = Logger.getLogger(Play.class.getName());
	
	public InjectedControllerFactory(Function <String,Object> instanciator) {
		this.instanciator = requireNonNull(instanciator);
		this.injector = new Injector(this.instanciator);
	}

	@Override
	public Object call(Class<?> clazz) {
		try {
			Object instance = clazz.getDeclaredConstructor().newInstance();
			SerializedProperties properties = loadPropertiesFor(clazz);
			
			return injector.injectFields(instance, properties);
			
		} catch (NoSuchMethodException | IllegalAccessException | SecurityException e) {
			LOGGER.log(Level.SEVERE, "Cannot access default constructor of {0} : {1}", new Object[] {clazz, e});
		} catch (IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			LOGGER.log(Level.SEVERE, "Failed to inject instance of {0} : {1}", new Object[] {clazz, e});
		}

		return null;
	}
	
	/** @return the properties stored in controller's config file */
	private SerializedProperties loadPropertiesFor(Class <?> controller) {
		SerializedProperties prop = new SerializedProperties();
		
		try( InputStream is = controller.getResourceAsStream(CONFIG_FILE) ) {
			if( is != null ) {
				prop = new SerializedProperties(is);
				prop.load();
			}
			
		} catch(IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to load the configuration file \"{0}\" : {1} ", new Object[] {CONFIG_FILE, e});
		}
		
		return prop;
	}

}
