package controllers;

import play.mvc.*;

public class AbstractApplication extends Controller {

    public static class HeaderKey {
        public static final String HEADER_AUTHENTICATION_TOKEN = "Authentication";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    public static class ParameterKey {
        public static final String STATUS = "status";
        public static final String MESSAGE = "message";
        public static final String ERROR = "error";
    }

    public static class FinderKey {
        public static final String ID = "_id";
        public static final String TOKEN = "token";
    }

}
