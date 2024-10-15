package com.doggydr.demo.servicio;

import java.util.Collection;
import java.util.List;

import com.doggydr.demo.entidad.Pet;
import com.doggydr.demo.entidad.Treatment;

public interface TreatmentService {
    
    public Treatment SearchById(Long id);

    public Collection<Treatment> SearchAll();

    public Treatment SearchByName(String name);
    
    public List<Treatment> SearchByVetId(Long id);
    
    public void DeleteById(Long id);

    public void update(Treatment client);
}
