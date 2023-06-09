package vn.mobileid.paperless.API;

public interface ISessionFactory {

    // auth/login
    IServerSession getServerSession() throws Exception;

    // auth/login
    IUserSession getUserSession(String username, String password);
}
