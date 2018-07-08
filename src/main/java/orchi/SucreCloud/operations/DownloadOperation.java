package orchi.SucreCloud.operations;

import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.*;

import orchi.SucreCloud.hdfs.HdfsManager;
import orchi.SucreCloud.hdfs.ZipFiles;

public class DownloadOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(DownloadOperation.class);
	private static FileSystem fs;

	public DownloadOperation() {
	}

	public DownloadOperation(AsyncContext ctx, JSONObject arg) {
		log.info("Nueva operacion de descarga.");
		fs = HdfsManager.getInstance().fs;
		String root = arg.getString("root");
		String path = arg.getString("path");
		Path opath = new Path(HdfsManager.newPath(root, path).toString());
		HttpServletResponse r = ((HttpServletResponse) ctx.getResponse());

		try {
			if (!fs.exists(opath)) {
				log.info("{} no existe",opath.toString());
				ctx.getResponse().getWriter().println("no exists" + opath.toString());
				log.info("Operacion de descarga terminada {}",opath.toString());
				ctx.complete();
			}
			if (fs.isFile(opath)) {
				log.info("Descargar archivo {}",opath.toString());
				FileStatus fileStatus = fs.getFileLinkStatus(opath);
				System.out.println("		" + fileStatus.getPath().getName());
				r.addHeader("Content-Disposition", " attachment; filename=\"" + fileStatus.getPath().getName() + "\"");

				HdfsManager.getInstance().readFile(opath, ctx.getResponse().getOutputStream());
				// ctx.getResponse().getWriter().println( opath.toString() );
				log.info("Operacion de descarga terminada {}",opath.toString());
				ctx.complete();
			}

			if (fs.isDirectory(opath)) {
				log.info("Descargar directorio {}",opath.toString());
				
				r.addHeader("Content-Disposition", " attachment; filename=\"" + opath.getName() + ".zip\"");

				new ZipFiles(new Tree(opath), ctx.getResponse().getOutputStream());
				
				log.info("Operacion de descarga terminada {}",opath.toString());
				// ctx.getResponse().getWriter().println("descargar carpeta");
				ctx.complete();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public JSONObject call() {

		return null;
	}

	public static class Tree {

		public ArrayList<String> dirs = new ArrayList<String>();;

		public Tree(Path path) {
			log.info("new  tree");
			get(path);

		}

		public void get(Path path) {
			log.info("get path {}",path.toString());
			try {
				for (FileStatus item : fs.listStatus(path)) {
					if (item.isFile()) {
						dirs.add(item.getPath().toString());
						log.info("\tadd to tree {}",item.getPath().toString());
					}
					if (item.isDirectory()) {
						get(item.getPath());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
