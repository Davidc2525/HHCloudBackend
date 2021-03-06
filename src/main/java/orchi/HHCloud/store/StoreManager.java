package orchi.HHCloud.store;

import orchi.HHCloud.*;
import orchi.HHCloud.provider.GetProvider;
import orchi.HHCloud.provider.ProviderManager;
import orchi.HHCloud.provider.ProviderManagerInstance;
import orchi.HHCloud.provider.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ProviderManager
public class StoreManager {
    public static final long SPACE_QUOTA_SIZE = Start.conf.getLong("store.storemanager.quote.users.verified");
    public static final long SPACE_QUOTA_SIZE_NO_VERIFIED_USER = Start.conf.getLong("store.storemanager.quote.users.unverified");
    private static Logger log = LoggerFactory.getLogger(StoreManager.class);
    private static String defaultStore = Start.conf.getString("store.storemanager.storeprovider");
    private static StoreManager instance = new StoreManager();
    private StoreProvider storeProvider = null;

    public StoreManager() {
        this(defaultStore);
    }

    public StoreManager(String storeProviderClassName) {
        log.debug("Creando proveedor de almacenamiento: {}", storeProviderClassName);
        try {
            @SuppressWarnings("unchecked")
            Class<? extends StoreProvider> ClassStore = (Class<? extends StoreProvider>) Class.forName(storeProviderClassName);

            storeProvider = ClassStore.newInstance();
            storeProvider.init();
            storeProvider.start();
            Providers.extractInterfaces(storeProvider);
            log.debug("Creado proveedor de almacenamiento: {}", storeProviderClassName);
        } catch (ClassNotFoundException e) {
            log.error("No se encontro la clase proveedora de almacenamiento: {}", storeProviderClassName);
            e.printStackTrace();
        } catch (InstantiationException e) {
            log.error("No se pudo iniciar el proveedor de almacenamiento: {}", storeProviderClassName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.error("No se pudo iniciar el proveedor de almacenamiento: {}", storeProviderClassName);
            e.printStackTrace();
        }
    }

    public StoreManager(Class<? extends StoreProvider> storeProviderClass) {
        log.debug("Creando proveedor de almacenamiento: {}", storeProviderClass.getName());
        try {
            Class<? extends StoreProvider> ClassStore = storeProviderClass;

            storeProvider = ClassStore.newInstance();
            storeProvider.init();
            storeProvider.start();
            log.debug("Creado proveedor de almacenamiento: {}", storeProviderClass.getName());
        } catch (InstantiationException e) {
            log.error("No se pudo iniciar el proveedor de almacenamiento: {}", storeProviderClass.getName());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.error("No se pudo iniciar el proveedor de almacenamiento: {}", storeProviderClass.getName());
            e.printStackTrace();
        }
    }

    @ProviderManagerInstance
    public static StoreManager getInstance() {
        log.debug("Obtener de StoreManager");
        if (instance == null) {
            log.warn("Creando nueva instancia de StoreManager con proveedor de almacenamiento por defecto: {}", defaultStore);
            instance = new StoreManager();
        }

        return instance;
    }

    @GetProvider
    public StoreProvider getStoreProvider() {
        log.debug("Obtener proveedor de almacenamiento: {}", storeProvider.getClass().getName());
        return storeProvider;
    }

}
