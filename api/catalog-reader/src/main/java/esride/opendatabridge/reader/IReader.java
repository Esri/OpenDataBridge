package esride.opendatabridge.reader;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sma
 * Date: 21.03.13
 * Time: 07:46
 * To change this template use File | Settings | File Templates.
 */
public interface IReader {

    public List<TransformedItem> getItemsFromCatalog() throws ReaderException;

}
