.PHONY: build clean docker-build docker-push test

IMAGE_NAME ?= log-sidecar
IMAGE_TAG ?= latest
REGISTRY ?= your-registry.com

build:
	mkdir -p build
	cd build && cmake .. && make -j$(shell nproc)

clean:
	rm -rf build

docker-build:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .

docker-push: docker-build
	docker tag $(IMAGE_NAME):$(IMAGE_TAG) $(REGISTRY)/$(IMAGE_NAME):$(IMAGE_TAG)
	docker push $(REGISTRY)/$(IMAGE_NAME):$(IMAGE_TAG)

test:
	@echo "Running tests..."
	# Add test commands here

debug:
	mkdir -p build
	cd build && cmake -DCMAKE_BUILD_TYPE=Debug .. && make -j$(shell nproc)

install-deps-alpine:
	apk add --no-cache build-base cmake curl-dev nlohmann-json pkgconfig linux-headers

install-deps-ubuntu:
	apt-get update && apt-get install -y \
		build-essential cmake libcurl4-openssl-dev nlohmann-json3-dev pkg-config
