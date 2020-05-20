package de.quantummaid.httpmaid.remotespecs.lambda;

import com.amazonaws.services.cloudformation.model.StackResource;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourcesLog {
    private final Map<String, List<String>> physicalIdsByLogicalId;

    public static ResourcesLog resourcesLog() {
        return new ResourcesLog(new HashMap<>());
    }

    public void log(final List<StackResource> resources) {
        resources.forEach(stackResource -> {
            final String logicalResourceId = stackResource.getLogicalResourceId();
            if (!physicalIdsByLogicalId.containsKey(logicalResourceId)) {
                physicalIdsByLogicalId.put(logicalResourceId, new ArrayList<>());
            }
            final List<String> physicalIds = physicalIdsByLogicalId.get(logicalResourceId);
            final String physicalResourceId = stackResource.getPhysicalResourceId();
            if (physicalIds.contains(physicalResourceId)) {
                System.out.printf("duplicate %s = %s%n", logicalResourceId, physicalResourceId);
            }
            physicalIds.add(physicalResourceId);
        });
    }

    public void dump() {
        physicalIdsByLogicalId.forEach((logicalId, physicalIds) -> {
            final String joinedPhysicalIds = String.join(" ", physicalIds);
            System.out.println(logicalId + ": " + joinedPhysicalIds);
        });
    }
}
