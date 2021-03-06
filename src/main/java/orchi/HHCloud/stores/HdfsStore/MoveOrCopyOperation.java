package orchi.HHCloud.stores.HdfsStore;

import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.Start;
import orchi.HHCloud.Util;
import orchi.HHCloud.share.ShareProvider;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.response.MoveOrCopyResponse;
import orchi.HHCloud.user.User;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class MoveOrCopyOperation implements IOperation {
    private static Logger log = LoggerFactory.getLogger(MoveOrCopyOperation.class);
    private boolean move;
    private Path srcPath;
    private Path dstPath;
    private String root;
    private Path srcpathWithRoot;
    private Path dstpathWithRoot;
    private FileSystem fs;
    private MoveOrCopyArguments args;
    private MoveOrCopyResponse response;
    private ShareProvider shp;

    public MoveOrCopyOperation(MoveOrCopyArguments arg) {
        response = new MoveOrCopyResponse();
        this.args = arg;
        this.move = arg.isMove();
        fs = HdfsManager.getInstance().fs;
        srcPath = new Path(args.getSrcPath().toString());
        dstPath = new Path(args.getDstPath().normalize().toString());
        root = args.getUserId();
        srcpathWithRoot = new Path(HdfsManager.newPath(root, srcPath.toString()).toString());
        dstpathWithRoot = new Path(HdfsManager.newPath(root, dstPath.toString()).toString());

        shp = Start.getShareManager().getShareProvider();
        log.debug("nueva operacion de {} ", move ? "mover ruta" : "copiar ruta");
    }

    public void setContextSrc(User user) {
        srcpathWithRoot = new Path(HdfsManager.newPath(user.getId(), srcPath.toString()).toString());
    }

    public void setContextDst(User user) {
        dstpathWithRoot = new Path(HdfsManager.newPath(user.getId(), dstPath.toString()).toString());
    }

    public MoveOrCopyResponse run() {
        boolean can = false;
        log.debug("	{} {} a {}", move ? "Moviendo" : "Copiando", srcPath.toString(), dstPath.toString());

        try {
            if (HdfsManager.getInstance().fs.exists(srcpathWithRoot)) {
                if (HdfsManager.getInstance().fs.exists(dstpathWithRoot)) {
                    response.setStatus("error");
                    response.setError("dstpath_is_used");
                    response.setMsg("la rruta de destino ya existe " + dstPath.toString());
                    log.debug("la rruta de destino ya existe {}", dstPath.toString());
                } else {

                    can = FileUtil.copy(fs, srcpathWithRoot, fs, dstpathWithRoot, move, true, fs.getConf());

                    if (can) {
                        response.setStatus("ok");
                        response.setPath(Paths.get(Util.getPathWithoutRootPath(srcpathWithRoot.toString())).normalize()
                                .getParent());

                        log.debug("	{} exitoso ", move ? "movido" : "copiado");

                        if (move) {
                            log.debug("{} eliminando de rrutas compartidas", args.getSrcPath());
                            shp.deleteShare(args.getUse(), args.getSrcPath(), true);
                        }
                    } else {
                        // (fs, srcpathWithRoot, dstpathWithRoot, move,
                        // fs.getConf());
                        response.setStatus("error");
                        response.setError("dstpath_is_used");
                        response.setMsg(String.format("no pudo {} ", move ? "mover" : "copiar"));

                        log.debug("no pudo {} ", move ? "mover" : "copiar");
                    }

                }

            } else {
                response.setStatus("error");
                response.setError("srcpath_no_found");
                response.setMsg(String.format("la ruta que quiere %s no existe", move ? "mover" : "copiar"));

                log.debug("	falla al {}, '{}' no existe", move ? "mover" : "copiar", srcPath.toString());
            }

        } catch (IOException e) {
            response.setStatus("error");
            response.setError("server_error");
            response.setMsg(e.getMessage());
            e.printStackTrace();
        }
        log.debug("operacion de {} terminada.", move ? "mover ruta" : "copiar rruta");
        return response;
    }

}
