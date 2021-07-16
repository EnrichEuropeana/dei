package pl.psnc.dei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Describes Project entity taken from the Transcription Platform
 * <p>
 * Created by pwozniak on 3/20/19
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project implements Serializable {

	@Id
	@GeneratedValue
	@JsonIgnore
	private long id;

	@JsonProperty("ProjectId")
	private String projectId;

	@JsonProperty("Name")
	private String name;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
	private List<Dataset> datasets = new ArrayList<>();

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "project")
	private Set<Record> records;

	@Override
	public String toString() {
		return this.getName();
	}
}
