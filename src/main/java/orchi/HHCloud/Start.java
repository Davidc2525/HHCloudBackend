package orchi.HHCloud;

import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration2.Configuration;
import org.apache.hadoop.fs.Path;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Api.ApiManager;
import orchi.HHCloud.Api.ApiFilter;
import orchi.HHCloud.Api.Auth.Auth;
import orchi.HHCloud.Api.Auth.Login;
import orchi.HHCloud.Api.Auth.Logout;
import orchi.HHCloud.Api.Fs.Fs;
import orchi.HHCloud.Api.Opener.Opener;
import orchi.HHCloud.Api.Uploader.Uploader;
import orchi.HHCloud.Api.User.Users;
import orchi.HHCloud.auth.AuthProvider;
import orchi.HHCloud.auth.DefaultAuthProvider;
import orchi.HHCloud.auth.logIO.LogInAndOut;
import orchi.HHCloud.conf.ConfManager;
import orchi.HHCloud.database.DbConnectionManager;
import orchi.HHCloud.mail.MailManager;
import orchi.HHCloud.store.StoreManager;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;
import orchi.HHCloud.user.UserManager;

/**
 * @Author david
 */
public class Start {
	private static Logger log = LoggerFactory.getLogger(Start.class);

	public static Server server;

	public static Configuration conf = ConfManager.getInstance().getConfig();

	public static void main(String[] args) throws Exception {

		int port = conf.getInt("api.port");
		String host = conf.getString("api.host");

		if(args.length==1){
			host = args[0];
		}
		if(args.length==2){
			host = args[0];
			port = Integer.valueOf(args[1]);
		}



		server = new Server();

		// HTTP Configuration
		HttpConfiguration http_config = new HttpConfiguration();

		http_config.setSecureScheme("https");
		http_config.setSecurePort(2525);
		http_config.setOutputBufferSize(32768);
		http_config.setRequestHeaderSize(8192);
		http_config.setResponseHeaderSize(8192);
		http_config.setSendServerVersion(false);
		http_config.setSendDateHeader(false);

		ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
		http.setPort(port);
		http.setHost(host);
		http.setIdleTimeout(Long.MAX_VALUE);

		server.addConnector(http);

		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);

		servletContext.setContextPath("/api");

		servletContext.addFilter(ApiFilter.class, "/*", null);

		servletContext.addServlet(ApiManager.addApi(Fs.class, "/fs"),"/fs");
		servletContext.addServlet(ApiManager.addApi(Users.class,"/user"), "/user");
		servletContext.addServlet(ApiManager.addApi(Auth.class,"/auth"), "/auth");
		servletContext.addServlet(ApiManager.addApi(Opener.class,"/opener"), "/opener");
		servletContext.addServlet(ApiManager.addApi(Uploader.class,"/uploader"), "/uploader");


		servletContext.addServlet(TEST.class, "/test").setAsyncSupported(true);

		ContextHandlerCollection contexts = new ContextHandlerCollection();

		contexts.setHandlers(new Handler[] { servletContext });

		server.setHandler(contexts);


		log.info("Iniciando servidor");
		server.start();
		ApiManager.showDescriptions();
		server.join();
	}

	public static UserManager getUserManager(){
		return UserManager.getInstance();
	}

	public static AuthProvider getAuthProvider(){
		return DefaultAuthProvider.getInstance();
	}

	public static LogInAndOut getLoginAndOut() {
		return LogInAndOut.getInstance();
	}

	public static StoreManager getStoreManager() {
		return StoreManager.getInstance();
	}

	public static DbConnectionManager getDbConnectionManager() {
		return DbConnectionManager.getInstance();
	}

	public static MailManager getMailManager(){
		return MailManager.getInstance();
	}
}
