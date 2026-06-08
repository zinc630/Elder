import http from "./http";

export type ElderProfileDto = {
  elderId: string;
  name: string;
  age: number | null;
  gender: string;
  address: string;
  keyHealthNotes: string;
};

export async function getElderProfile(elderId: string): Promise<ElderProfileDto> {
  const resp = await http.get(`/v1/elders/${elderId}/profile`);
  return resp.data.data;
}

export async function listElders(): Promise<ElderProfileDto[]> {
  const resp = await http.get(`/v1/elders`);
  return resp.data.data ?? [];
}

export type ElderProfileUpsertRequest = {
  name: string;
  gender: string;
  address: string;
  age: number;
  keyHealthNotes?: string;
};

export async function upsertElderProfile(elderId: string, req: ElderProfileUpsertRequest): Promise<ElderProfileDto> {
  const resp = await http.post(`/v1/elders/${elderId}/profile`, req);
  return resp.data.data;
}

export type NearbyHospitalDto = {
  id: string;
  name: string;
  address: string;
  distance: string;
  phone: string;
};

export type NearbyDoctorDto = {
  id: string;
  name: string;
  title: string;
  hospital: string;
  specialty: string;
  phone: string;
};

export type NearbyMedicalResponse<T> = {
  source: string;
  userAddress: string;
  regionLabel: string;
  location: string;
  items: T[];
};

export async function listNearbyHospitals(elderId: string): Promise<NearbyMedicalResponse<NearbyHospitalDto>> {
  const resp = await http.get(`/v1/elders/${elderId}/nearby-hospitals`);
  return resp.data.data;
}

export async function listNearbyDoctors(elderId: string): Promise<NearbyMedicalResponse<NearbyDoctorDto>> {
  const resp = await http.get(`/v1/elders/${elderId}/nearby-doctors`);
  return resp.data.data;
}

