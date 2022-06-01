package cc.blynk.server.handlers.common;

import cc.blynk.server.Holder;
import cc.blynk.server.core.dao.FileManager;
import cc.blynk.server.core.dao.ReportingDiskDao;
import cc.blynk.server.core.dao.SessionDao;
import cc.blynk.server.core.dao.UserDao;
import cc.blynk.server.core.dao.UserKey;
import cc.blynk.server.core.model.auth.User;
import cc.blynk.server.core.protocol.exceptions.IllegalCommandException;
import cc.blynk.server.core.protocol.model.messages.StringMessage;
import cc.blynk.server.db.DBManager;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cc.blynk.server.internal.CommonByteBufUtil.ok;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
public final class DeleteUserLogic {

    private static final Logger log = LogManager.getLogger(DeleteUserLogic.class);

    private final UserDao userDao;

    private final ReportingDiskDao reportingDiskDao;

    private final FileManager fileManager;

    private final DBManager dbManager;

    private final SessionDao sessionDao;

    public DeleteUserLogic(Holder holder) {
        this.userDao = holder.userDao;
        this.fileManager = holder.fileManager;
        this.reportingDiskDao = holder.reportingDiskDao;
        this.dbManager = holder.dbManager;
        this.sessionDao = holder.sessionDao;
    }

    public void messageReceived(ChannelHandlerContext ctx, User user, StringMessage msg) {
        UserKey userKey = new UserKey(user.email, user.appName);
        User removeUser = userDao.delete(userKey);
        if (removeUser == null) {
            throw new IllegalCommandException("Can't find user for removal.");
        }

        fileManager.delete(user.email, user.appName);
        reportingDiskDao.delete(user);
        dbManager.deleteUser(userKey);
        sessionDao.userSession.remove(userKey);

        log.info("User {} successfully removed.", user.email);
        ctx.writeAndFlush(ok(msg.id), ctx.voidPromise());
    }

}
