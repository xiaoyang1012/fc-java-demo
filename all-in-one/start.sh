docker run --cap-add=SYS_PTRACE \
    --security-opt seccomp=unconfined \
    -it -e fc_max_server_heap_size='115m' \
    -e fc_min_server_heap_size='38m' \
    -v /Users/santi/code/ws_eclipse/fc-java-demo/code:/code \
    -p 9000:9000 \
    -p 9001:9001 \
    -m 512m \
    -e fc_enable_new_java_ca=true \
    reg.docker.alibaba-inc.com/serverless/runtime-java8:refine-java-ca bash
