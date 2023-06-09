package vn.mobileid.paperless.Model;

import vn.mobileid.paperless.API.IServerSession;
import vn.mobileid.paperless.API.ISessionFactory;
import vn.mobileid.paperless.API.IUserSession;
import vn.mobileid.paperless.API.Property;

public class SessionFactory implements ISessionFactory {

    private Property prop;
    private String lang;
    private String username;
    private String password;

    public SessionFactory(Property prop, String lang) {
        this.prop = prop;
        this.lang = lang;
    }

    @Override
    public IServerSession getServerSession() throws Exception {
        ServerSession serverSession = new ServerSession(this.prop, this.lang);
        // serverSession = new ServerSession(this.prop, this.lang);
        return serverSession;
    }

    @Override
    public IUserSession getUserSession(String username, String password) {
        return null;
    }

}
