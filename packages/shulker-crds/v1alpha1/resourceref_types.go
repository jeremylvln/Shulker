package v1alpha1

type ResourceRef struct {
	// Direct URL of the resource to download.
	// +optional
	Url string `json:"url,omitempty"`

	// Source of the resource URL. Cannot be used if value is not
	// empty.
	// +optional
	UrlFrom *ResourceRefSource `json:"urlFrom,omitempty"`
}

type ResourceRefSource struct {
	// Reference to a Maven artiact to use as source.
	// +optional
	MavenRef *ResourceRefMavenSelector `json:"mavenRef,omitempty"`
}

type ResourceRefMavenSelector struct {
	// URL to the Maven repository to download the artifact from.
	//+kubebuilder:validation:Required
	Repository string `json:"repository,omitempty"`

	// Group ID of the Maven artifact to download.
	//+kubebuilder:validation:Required
	GroupId string `json:"groupId,omitempty"`

	// Artifact ID of the Maven artifact to download.
	//+kubebuilder:validation:Required
	ArtifactId string `json:"artifactId,omitempty"`

	// Version of the Maven artifact to download.
	//+kubebuilder:validation:Required
	Version string `json:"version,omitempty"`

	// Name of the Kubernetes Secret containing the repository
	// credentials. The secret must contains a username
	// and password keys.
	//+optional
	CredentialsSecretName string `json:"credentialsSecretName,omitempty"`
}
