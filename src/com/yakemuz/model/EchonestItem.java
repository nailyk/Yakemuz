package com.yakemuz.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.yakemuz.echonestAPI.EchonestAPI;
import com.yakemuz.echonestAPI.EchonestException;
import com.yakemuz.echonestAPI.MQuery;
import com.yakemuz.echonestAPI.Params;

public class EchonestItem {

	protected Map<String, Object> data;
	protected EchonestAPI en;
	private String type;
	private String path;
	private String originalID = null;
	private String id;

	EchonestItem(EchonestAPI en, String type, String path, Map<String, Object> data)
			throws EchonestException {
		this.en = en;
		this.type = type;
		this.path = path;
		this.data = data;
		this.id = findID();
		extractForeignIDs(data);
	}

	EchonestItem(EchonestAPI en, String type, String path, String id) throws EchonestException, IOException {
		this.en = en;
		this.type = type;
		this.path = path;
		originalID = id;
		this.id = id;
		refresh();
	}

	EchonestItem(EchonestAPI en, String type, String path, String nameOrID, boolean byName) throws EchonestException, IOException {
		this.en = en;
		this.type = type;
		this.path = path;
		if (byName) {
			this.data = getItemMapByName(nameOrID, null);
		} else {
			originalID = nameOrID;
			this.data = getItemMap(nameOrID);
		}
		this.id = findID();
		extractForeignIDs(data);
	}

	EchonestItem(EchonestAPI en, String type, String path, String id, String idType) throws EchonestException, IOException {
		this.en = en;
		this.type = type;
		this.path = path;
		originalID = id;
		this.data = getItemMap(id, null, idType);
		this.id = findID();
		extractForeignIDs(data);
	}

	private String findID() throws EchonestException {
		String alt_name = type + "_" + "id";
		if (data.get("id") != null) {
			return (String) data.get("id");
		} else if (data.get(alt_name) != null) {
			return (String) data.get(alt_name);
		} else {
			throw new EchonestException(
					EchonestException.ERR_MISSING_PARAMETER, "Missing ID");
		}
	}

	protected final void refresh() throws EchonestException, IOException {
		this.data = getItemMap(this.id);
		this.id = findID();
		extractForeignIDs(data);
	}

	@Override
	public String toString() {
		return data.toString();
	}


	public void fetchBuckets(String[] bucket) throws EchonestException, IOException {
		fetchBuckets(bucket, false);
	}

	/**
	 * Determines if the item has the given bucket. This is used primarily for
	 * testing
	 *
	 * @param bucket the name of the bucket
	 * @return true if the item has the data associated with the given bucket.
	 */
	public boolean hasBucket(String bucket) {
		return data.containsKey(bucket);
	}

	public void fetchBuckets(String[] bucket, boolean force) throws EchonestException, IOException {
		List<String> buckets = new ArrayList<String>();

		for (String s : bucket) {
			if (force || !data.containsKey(s)) {
				buckets.add(s);
			}
		}
		if (buckets.size() > 0) {
			Map<String, Object> map = getItemMap(getID(), buckets);
			extractForeignIDs(map);
			for (String s : buckets) {
				Object value = map.get(s);
				if (value != null) {
					data.put(s, value);
				}
			}
		}
	}

	public void fetchBucket(String bucket) throws EchonestException, IOException {
		fetchBucket(bucket, bucket);
	}

	public void fetchBucket(String bucket, boolean force) throws EchonestException, IOException {
		fetchBucket(bucket, bucket, force);
	}

	protected void fetchBucket(String paramName, String bucketName) throws EchonestException, IOException {
		fetchBucket(paramName, bucketName, false);
	}

	protected void fetchBucket(String paramName, String bucketName, boolean force) throws EchonestException, IOException {
		if (force || !data.containsKey(bucketName)) {
			Map<String, Object> map = getItemMap(getID(), paramName);
			// special case for foreign_ids
			extractForeignIDs(map);
					Object value = map.get(bucketName);
					if (value != null) {
						data.put(bucketName, value);
					}
		}
	}

	private void extractForeignIDs(Map<String, Object> map) {
		List<?> idList = (List<?>) map.get("foreign_ids");
		if (idList != null) {
			for (Object o : idList) {
				if (o instanceof String) {
					String fullID = (String) o;
					String catalog = fullID.split(":")[0];
					data.put(catalog, fullID);
				} else {
					Map<?, ?> idMap = (Map<?, ?>) o;
					String catalog = (String) idMap.get("catalog");
					String fid = (String) idMap.get("foreign_id");
					data.put(catalog, fid);
				}
			}
		}
	}

	public String getID() {
		return id;
	}

	/**
	 * Returns a string at the given path within the item. Paths are of the
	 * form: 'key', or 'key[0]', or 'key1.key2[0].key3'
	 *
	 * @param path the path
	 * @return the value at the path or null.
	 */
	public String getString(String path) {
		return (String) getObject(path);
	}

	public Object getObject(String path) {
		MQuery mq = new MQuery(data);
		return mq.getObject(path);
	}

	public Double getDouble(String path) {
		Object val = getObject(path);
		if (val != null) {
			// BUG: workaround API bug
			if (val instanceof List) {
				val = ((List<?>) val).get(0);
			}
			return ((Number) val).doubleValue();
		} else {
			return Double.NaN;
		}
	}

	public Integer getInteger(String path) {
		Number val = (Number) getObject(path);
		return Integer.valueOf(val.intValue());
	}

	public final Map<String, Object> getItemMap(String id) throws EchonestException, IOException {
		return getItemMap(id, (String) null);
	}

	public Map<String, Object> getItemMap(String id, String bucket) throws EchonestException, IOException {
		return getItemMap(id, bucket, "id");
	}

	public final Map<String, Object> getItemMapByName(String name, String bucket) throws EchonestException, IOException {
		return getItemMap(name, bucket, "name");
	}

	@SuppressWarnings("unchecked")
	public final Map<String, Object> getItemMap(String id, String bucket, String idType)
			throws EchonestException, IOException {
		Params p = new Params();
		p.add(idType, id);

		if (bucket != null) {
			p.add("bucket", bucket);
		}

		Map<?, ?> results = en.getCmd().sendCommand(type + "/profile", p);
		Map<?, ?> response = (Map<?, ?>) results.get("response");
		MQuery mq = new MQuery(response);
		return (Map<String, Object>) mq.getObject(path);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getItemMap(String id, List<String> buckets) throws EchonestException, IOException {
		Params p = new Params();
		p.add("id", id);

		if (buckets != null) {
			p.add("bucket", buckets);
		}

		Map<?, ?> results = en.getCmd().sendCommand(type + "/profile", p);
		Map<?, ?> response = (Map<?, ?>) results.get("response");
		MQuery mq = new MQuery(response);
		return (Map<String, Object>) mq.getObject(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EchonestItem other = (EchonestItem) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public String getOriginalID() {
		return originalID;
	}
}
