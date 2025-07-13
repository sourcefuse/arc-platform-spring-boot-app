# Spring Boot Application Helm Chart

This Helm chart deploys a Spring Boot application on a Kubernetes cluster.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.2.0+

## Installing the Chart

To install the chart with the release name `my-release`:

```bash
helm install my-release ./spring-boot-app
```

The command deploys the Spring Boot application on the Kubernetes cluster with default configuration. The [Parameters](#parameters) section lists the parameters that can be configured during installation.

## Uninstalling the Chart

To uninstall/delete the `my-release` deployment:

```bash
helm uninstall my-release
```

This removes all the Kubernetes components associated with the chart and deletes the release.

## Upgrading the Chart

To upgrade the `my-release` deployment:

```bash
helm upgrade my-release ./spring-boot-app
```

## Parameters

### Global parameters

| Name                      | Description                                     | Value |
| ------------------------- | ----------------------------------------------- | ----- |
| `nameOverride`            | String to partially override common.names.fullname | `""` |
| `fullnameOverride`        | String to fully override common.names.fullname  | `""` |

### Common parameters

| Name                | Description                                        | Value           |
| ------------------- | -------------------------------------------------- | --------------- |
| `replicaCount`      | Number of replicas                                 | `1`             |
| `image.repository`  | Image repository                                   | `sample-spring-boot-app` |
| `image.tag`         | Image tag                                          | `latest`        |
| `image.pullPolicy`  | Image pull policy                                  | `IfNotPresent`  |
| `imagePullSecrets`  | Image pull secrets                                 | `[]`            |

### Service parameters

| Name                      | Description                                     | Value       |
| ------------------------- | ----------------------------------------------- | ----------- |
| `service.type`            | Service type                                    | `ClusterIP` |
| `service.port`            | Service port                                    | `8080`      |

### Ingress parameters

| Name                      | Description                                     | Value       |
| ------------------------- | ----------------------------------------------- | ----------- |
| `ingress.enabled`         | Enable ingress                                  | `false`     |
| `ingress.className`       | Ingress class name                              | `""`       |
| `ingress.annotations`     | Ingress annotations                             | `{}`        |
| `ingress.hosts`           | Ingress hosts                                   | See values.yaml |
| `ingress.tls`             | Ingress TLS configuration                       | `[]`        |

### Resource parameters

| Name                      | Description                                     | Value       |
| ------------------------- | ----------------------------------------------- | ----------- |
| `resources.limits.cpu`    | CPU limits                                      | `1000m`     |
| `resources.limits.memory` | Memory limits                                   | `1024Mi`    |
| `resources.requests.cpu`  | CPU requests                                    | `500m`      |
| `resources.requests.memory` | Memory requests                               | `512Mi`     |

### Autoscaling parameters

| Name                                          | Description                                     | Value   |
| --------------------------------------------- | ----------------------------------------------- | ------- |
| `autoscaling.enabled`                         | Enable autoscaling                              | `false` |
| `autoscaling.minReplicas`                     | Minimum number of replicas                      | `1`     |
| `autoscaling.maxReplicas`                     | Maximum number of replicas                      | `10`    |
| `autoscaling.targetCPUUtilizationPercentage`  | Target CPU utilization percentage               | `80`    |

### Other parameters

| Name                      | Description                                     | Value   |
| ------------------------- | ----------------------------------------------- | ------- |
| `podDisruptionBudget.enabled` | Enable PodDisruptionBudget                   | `true`  |
| `podDisruptionBudget.minAvailable` | Minimum available pods                  | `1`     |
| `networkPolicy.enabled`   | Enable NetworkPolicy                            | `true`  |
| `configMap.enabled`       | Enable ConfigMap                                | `true`  |
| `secret.enabled`          | Enable Secret                                   | `true`  |
