package me.snowdrop.istio.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import me.snowdrop.istio.api.DoneableIstioResource;
import me.snowdrop.istio.api.IstioResource;
import me.snowdrop.istio.api.internal.IstioSpecRegistry;

@Deprecated
public class KubernetesAdapter implements Adapter {

    private KubernetesClient client;

    public KubernetesAdapter(KubernetesClient kubernetesClient) {
        this.client = kubernetesClient;
    }

    public List<IstioResource> createCustomResources(IstioResource... resources) {
        if(resources != null) {
            List<IstioResource> results = new ArrayList<>(resources.length);

            for (IstioResource resource : resources) {
                final CustomResourceDefinition customResourceDefinition = getCustomResourceDefinition(resource);

                final IstioResource result = client.customResources(customResourceDefinition, IstioResource.class, KubernetesResourceList.class, DoneableIstioResource.class)
                        .inNamespace(client.getNamespace())
                        .create(resource);
                results.add(result);
            }

            return results;
        }

        return Collections.emptyList();
    }

    public List<IstioResource> createOrReplaceCustomResources(IstioResource... resources) {
        if(resources != null) {
            List<IstioResource> results = new ArrayList<>(resources.length);

            for (IstioResource resource : resources) {
                final CustomResourceDefinition customResourceDefinition = getCustomResourceDefinition(resource);

                final IstioResource result = client.customResources(customResourceDefinition, IstioResource.class, KubernetesResourceList.class, DoneableIstioResource.class)
                        .inNamespace(client.getNamespace())
                        .createOrReplace(resource);
                results.add(result);
            }

            return results;
        }

        return Collections.emptyList();
    }

    public Boolean deleteCustomResources(IstioResource resource) {

        if (resource != null) {
            final CustomResourceDefinition customResourceDefinition =
                getCustomResourceDefinition(resource);

            return
                client.customResources(customResourceDefinition, IstioResource.class, KubernetesResourceList.class,
                    DoneableIstioResource.class)
                    .inNamespace(client.getNamespace())
                    .delete(resource);
        }

        return false;
    }

    private CustomResourceDefinition getCustomResourceDefinition(IstioResource resource) {
        final String kind = resource.getKind();
        final String crdName = IstioSpecRegistry.getCRDNameFor(kind).orElseThrow(() -> new IllegalArgumentException(String.format("%s is not a known Istio resource.", kind)));
        final CustomResourceDefinition customResourceDefinition = client.customResourceDefinitions().withName(crdName).get();
        if (customResourceDefinition == null) {
            throw new IllegalArgumentException(String.format("Custom Resource Definition %s is not found in cluster %s",
                crdName, client.getMasterUrl()));
        }
        return customResourceDefinition;
    }

    @Override
    public KubernetesClient getKubernetesClient() {
        return client;
    }
}
