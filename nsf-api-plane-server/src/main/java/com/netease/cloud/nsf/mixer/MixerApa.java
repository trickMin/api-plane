package com.netease.cloud.nsf.mixer;

import io.grpc.stub.StreamObserver;
import net.devh.springboot.autoconfigure.grpc.server.GrpcService;
import nsfmeta.HandleNsfmetaServiceGrpc;
import nsfmeta.TemplateHandlerService;


/**
 * Created by 张武(zhangwu@corp.netease.com) at 2020/2/4
 */
@GrpcService(HandleNsfmetaServiceGrpc.class)
public class MixerApa extends HandleNsfmetaServiceGrpc.HandleNsfmetaServiceImplBase {

	@Override
	public void handleNsfmeta(TemplateHandlerService.HandleNsfmetaRequest request, StreamObserver<TemplateHandlerService.OutputMsg> responseObserver) {
		TemplateHandlerService.InstanceMsg instance = request.getInstance();
		String sourceUid = instance.getSourceUid();
		String destinationUid = instance.getDestinationUid();
		TemplateHandlerService.OutputMsg output = TemplateHandlerService.OutputMsg.newBuilder()
			.setDestinationProject("dProj")
			.setSourceProject("sProj")
			.build();
		responseObserver.onNext(output);
		responseObserver.onCompleted();
	}
}

