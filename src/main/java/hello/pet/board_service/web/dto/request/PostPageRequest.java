package hello.pet.board_service.web.dto.request;

public record PostPageRequest(
	int page,
	int size
) {
}
