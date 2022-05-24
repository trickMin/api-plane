applyTo: HTTP_FILTER
match:
  context: GATEWAY
  listener:
    filterChain:
      filter:
        name: envoy.filters.network.http_connection_manager
        subFilter:
          name: envoy.filters.http.router
    portNumber: ${portNumber}
patch:
  operation: INSERT_BEFORE
  value:
    name: envoy.filters.http.grpc_json_transcoder
    typed_config:
      "@type": type.googleapis.com/envoy.extensions.filters.http.grpc_json_transcoder.v3.GrpcJsonTranscoder
      proto_descriptor_bin: ${proto_descriptor_bin}
      services:
      <#list services as p>
        -
        <@indent count=14>${p}</@indent>
      </#list>
      print_options:
        add_whitespace: true
        always_print_primitive_fields: true
        always_print_enums_as_ints: true
        preserve_proto_field_names: false