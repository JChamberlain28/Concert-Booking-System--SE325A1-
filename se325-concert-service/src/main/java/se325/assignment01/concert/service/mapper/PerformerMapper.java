package se325.assignment01.concert.service.mapper;

import se325.assignment01.concert.common.dto.PerformerDTO;
import se325.assignment01.concert.service.domain.Performer;

public class PerformerMapper {

    public static PerformerDTO convertToDTO(Performer performer){

        // create dto using field info contained in a performer object
        return new PerformerDTO(performer.getId(), performer.getName(),
                performer.getImageName(), performer.getGenre(), performer.getBlurb());

    }
}
