package security;

import play.Application;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.csrf.CSRFFilter;

public class UWGlobal extends GlobalSettings {

    @Override
    public void onStart(Application app) {
        super.onStart(app);
    }

    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[] { CSRFFilter.class }; // CROSS SITE REQUEST FORGERY
    }

}
