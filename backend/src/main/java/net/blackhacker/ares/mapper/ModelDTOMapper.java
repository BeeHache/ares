package net.blackhacker.ares.mapper;

public interface ModelDTOMapper <MODEL, DTO>{
    DTO toDTO(MODEL model);
    MODEL toModel(DTO dto);
}
