import http from "./http";

export type ChildProfileDto = {
  childId: string;
  name: string;
  age: number | null;
  gender: string;
  address: string;
  relationDesc: string;
};

export type ChildProfileUpsertRequest = {
  name: string;
  gender: string;
  address?: string;
  age: number;
  relationDesc?: string;
};

export async function getChildProfile(childId: string): Promise<ChildProfileDto> {
  const resp = await http.get(`/v1/children/${childId}/profile`);
  return resp.data.data;
}

export async function upsertChildProfile(
  childId: string,
  req: ChildProfileUpsertRequest
): Promise<ChildProfileDto> {
  const resp = await http.post(`/v1/children/${childId}/profile`, req);
  return resp.data.data;
}
