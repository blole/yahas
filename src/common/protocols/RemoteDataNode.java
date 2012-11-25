package common.protocols;

/**
 * Interface representing a DataNode. 
 * It inturn implements the relevant procotocols
 * 
 * @author Bjorn, Govind, Jerry, Karan
 * 
 */
public interface RemoteDataNode extends ClientDataNodeProtocol,
		NameNodeDataNodeProtocol, DataNodeDataNodeProtocol {
}
