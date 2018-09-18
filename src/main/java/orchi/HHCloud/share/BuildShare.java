/**
 * BuildShare.java
 */
package orchi.HHCloud.share;

import orchi.HHCloud.user.User;

import java.nio.file.Path;

/**
 * @author david 14 ago. 2018
 */
public abstract class BuildShare {
    public static Share createShare(String id, User owner, Path path, long createdAt) {
        Share s = new Share();
        s.setId(id);
        s.setOwner(owner);
        s.setPath(path);
        s.setSharedAt(createdAt);
        return s;
    }

}
