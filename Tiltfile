default_registry('localhost:5000')

# apple silicon
platform = 'linux/arm64'
gradlew = "./gradlew"
build_services = "chmod +x %s && %s generateProto && %s build -x test" % (gradlew, gradlew, gradlew)

if os.name == "nt":
    gradlew = "gradlew.bat"
    platform = 'linux/amd64' # For Intel
    build_services = "%s generateProto && %s build -x test" % (gradlew, gradlew)

if os.path.exists('jwt-secret.yaml'):
    print("🔒 Applying jwt secret")
    k8s_yaml('jwt-secret.yaml')

if os.path.exists('ses-secret.yaml'):
    print("🔒 Applying ses secret")
    k8s_yaml('ses-secret.yaml')

if os.path.exists('secrets-rbac.yaml'):
    print("🔒 Applying secret rbac")
    k8s_yaml('secrets-rbac.yaml')

local_resource(
    'api-gateway-build',
    cmd='%s build -x test' % gradlew,
    dir='./api-gateway',
    deps=['./api-gateway/build.gradle', './api-gateway/src/main']
)

local_resource(
    'user-service-build',
    cmd=build_services,
    dir='./user-service',
    deps=['./user-service/build.gradle', './user-service/src/main']
)

local_resource(
    'client-service-build',
    cmd=build_services,
    dir='./client-service',
    deps=['./client-service/build.gradle', './client-service/src/main']
)

local_resource(
    'comms-service-build',
    cmd=build_services,
    dir='./comms-service',
    deps=['./comms-service/build.gradle', './comms-service/src/main']
)

local_resource(
    'logs-service-build',
    cmd=build_services,
    dir='./logs-service',
    deps=['./logs-service/build.gradle', './logs-service/src/main']
)

modules = [
    {
        "base_repo": "user-service",
        "values": "dev.values.yaml",
        "port_mapping": "8080:8080"
    },
    {
        "base_repo": "client-service",
        "values": "dev.values.yaml",
        "port_mapping": "8081:8081"
    },
    {  # Fix this indentation
        "base_repo": "logs-service",
        "values": "dev.values.yaml",
        "port_mapping": "8082:8082"
    },
    {
        "base_repo": "comms-service",
        "values": "dev.values.yaml",
        "port_mapping": "8083:8083"
    },
    {
        "base_repo": "api-gateway",
        "values": "dev.values.yaml",
        "port_mapping": "9000:9000"
    }
]

namespace = "default"

print("⚙️ Provisioning infra")

# provision kafka + schema registry
k8s_yaml(helm("./k8s/charts/schema-registry/", name="schema-registry", values="./k8s/charts/schema-registry/values.yaml"))
k8s_resource(workload="schema-registry", port_forwards='8000:8081')
k8s_resource(workload="schema-registry-kafka-controller", port_forwards='9092:9092')

# provision the db
k8s_yaml(helm("./k8s/charts/postgresql/",name="scrooge-bank", namespace=namespace, values="./k8s/charts/postgresql/values.yaml"))
k8s_resource(workload="scrooge-bank-postgresql", port_forwards='5432:5432', resource_deps=["schema-registry"])

# provision mongo
k8s_yaml(helm("./k8s/charts/mongo/",name="scrooge-bank", namespace=namespace, values="./k8s/charts/mongo/values.yaml"))
k8s_resource(workload="scrooge-bank-mongo", port_forwards='27017:27017', resource_deps=["scrooge-bank-postgresql"])

# provision redis
k8s_yaml(helm("./k8s/charts/redis/", name="redis", values="./k8s/charts/redis/values.yaml"))
k8s_resource(workload="redis-master", port_forwards='6379:6379', resource_deps=["schema-registry"])

k8s_yaml('./scripts/monitoring-stack.yaml')
k8s_resource(workload="loki", port_forwards='3100:3100', resource_deps=["schema-registry"])
k8s_resource(workload="prometheus", port_forwards='9090:9090', resource_deps=["loki"])
k8s_resource(workload="grafana", port_forwards='3000:3000', resource_deps=["prometheus"])

count = 0
for module in modules:
    # Specify image name, start of path, relative path to Dockerfile
    ref = module["base_repo"] + "/dev"
    context = "./" + module["base_repo"]
    dockerfile = context + "/Dockerfile"
    chart = "k8s/charts/" + module["base_repo"] + "/"
    values = chart + module["values"]

    print('📦 Building %s for platform: %s' % (module["base_repo"], platform))
    docker_build(
        ref,
        context,
        dockerfile=dockerfile,
        live_update=[
            sync(context + "/src/main/java", '/app')
        ],
        extra_tag=["latest"],
        platform=platform
    )

    k8s_yaml(helm(chart, name=module["base_repo"], namespace=namespace, values=values))
    k8s_resource(workload=module["base_repo"], port_forwards=module["port_mapping"], resource_deps=["scrooge-bank-postgresql", "schema-registry", "redis-master", "scrooge-bank-mongo"])

k8s_resource(workload="user-service", resource_deps=["client-service", "comms-service"])
k8s_resource(workload="logs-service", resource_deps=["client-service"])
k8s_resource(workload="comms-service", resource_deps=["client-service"])
