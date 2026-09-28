package com.ventanago.region.service.mapper;

import com.ventanago.region.repository.entity.Region;
import com.ventanago.region.service.dto.RegionDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    List<RegionDto> toDtoList(List<Region> regiones);

    RegionDto toDto(Region region);

    Region toEntity(RegionDto regionDto);

}
