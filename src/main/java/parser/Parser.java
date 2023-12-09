package parser;
import models.bgpls.UpdateMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class Parser {
    public abstract List<UpdateMessage> readMessage(InputStream input) throws IOException;
}
