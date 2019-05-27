package pl.psnc.dei.response.search;

import java.util.List;

public interface Item {

	String getId();

	void setId(String id);

	List<String> getTitle();

	void setTitle(List<String> title);

	String getAuthor();

	void setAuthor(String author);

	String getDataProviderInstitution();

	void setDataProviderInstitution(String dataProvider);

	List<String> getLanguage();

	void setLanguage(List<String> language);

	List<String> getRights();

	void setRights(List<String> rights);

	String getThumbnailURL();

	void setThumbnailURL(String edmPreview);

	String getSourceObjectURL();

	void setSourceObjectURL(String guid);
}
